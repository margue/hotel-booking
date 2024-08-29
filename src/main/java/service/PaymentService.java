package service;

import persistence.*;

import java.util.*;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingsRepository bookingsRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.roomRepository = new RoomRepository();
        this.invoiceRepository = new InvoiceRepository();
        this.bookingsRepository = new BookingsRepository();
    }

    public PaymentService(PaymentRepository paymentRepository, RoomRepository roomRepository, InvoiceRepository invoiceRepository, BookingsRepository bookingsRepository) {
        this.paymentRepository = paymentRepository;
        this.roomRepository = roomRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookingsRepository = bookingsRepository;
    }

    /*
    Postcondition: Guest has paid a certain amount to the hotel.
     */
    public void payAmount(GuestName guestName, Amount amount){
        List<Payment> guestPayments = paymentRepository.load(guestName);
        guestPayments.add(new Payment(guestName, amount));
        paymentRepository.save(guestName, guestPayments);
    }

    public Amount remainingCredit(GuestName guestName){
        List<Payment> payments = paymentRepository.load(guestName);
        return remainingCredit(payments);
    }

    private static Amount remainingCredit(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getRemainingCredit)
                .reduce(Amount.ZERO, Amount::add);
    }

    /*
    Precondition: For all room numbers passed to the method there must be a booking that can be invoiced.
    Precondition: The guest must have paid enough to the hotel up-front.

    Postcondition: An invoice has been created and stored in the InvoiceRepository.
    Postcondition: The guest's payments are reduced by the amount of the invoice.
    Postcondition: The invoiced bookings are marked as invoiced.
     */
    public Either<Error,Invoice> produceInvoice(GuestName guestName, DepartureDate departureDate, List<RoomNumber> roomNumbers) {
        List<Booking> unpaidBookings = bookingsRepository.getNonInvoicedBookingsFor(guestName, departureDate)
                .stream().filter(r -> roomNumbers.contains(r.getRoomNumber())).toList();
        if (unpaidBookings.size() == 0) {
            return Either.ofError(new Error(String.format("No bookings to be invoiced for given customer " +
                    "'%s', departureDate [%s] and roomNumbers %s", guestName.guestName(), departureDate, roomNumbers)));
        }
        Amount totalAmount =
                unpaidBookings.stream()
                        .map(Booking::getTotalAmount)
                        .reduce(Amount.ZERO, Amount::add);
        List<Payment> payments = paymentRepository.load(guestName);
        Amount credit = remainingCredit(payments);
        if(totalAmount.isMoreThan(credit)){
            return Either.ofError(new Error("Payment insufficient. Necessary payment: " + (totalAmount.subtract(credit))));
        }

        payments.sort(Payment::compareByPaymentDate);
        Amount remainingTotalAmount = totalAmount;
        for (Payment payment: payments){
            if(remainingTotalAmount.isMoreThan(Amount.ZERO)){
                Amount remainingCreditForPayment = payment.getRemainingCredit();
                if(remainingCreditForPayment.isMoreThanOrEqual(remainingTotalAmount)){
                    payment.reduceCreditBy(remainingTotalAmount);
                    break;
                } else {
                    payment.reduceCreditBy(remainingCreditForPayment);
                    remainingTotalAmount = remainingTotalAmount.subtract(remainingCreditForPayment);
                }
            } else {
                break;
            }
        }
        paymentRepository.save(guestName, payments);

        unpaidBookings.forEach(Booking::markAsInvoiced);
        bookingsRepository.updateAll(unpaidBookings);

        Invoice invoice = new Invoice(new InvoiceId(UUID.randomUUID().toString()), guestName, unpaidBookings, totalAmount);

        invoiceRepository.save(invoice);

        return Either.ofResult(invoice);
    }

}
