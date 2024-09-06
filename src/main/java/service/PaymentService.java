package service;

import persistence.*;

import java.util.*;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingsRepository bookings;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = new InvoiceRepository();
        this.bookings = new BookingsRepository();
    }

    public PaymentService(PaymentRepository paymentRepository, RoomRepository roomRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookings = new BookingsRepository();
        roomRepository.getRooms().values().forEach(room -> this.bookings.save(room.getBookingsForRoom()));
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
        List<BookingsForRoom> bookingsForBookedRooms = bookings.getBookingsForRooms(roomNumbers);
        Map<RoomNumber, List<Booking>> nonInvoicedBookingsForRooms = new HashMap<>();
        bookingsForBookedRooms.forEach(bookings -> {
            nonInvoicedBookingsForRooms.put(bookings.roomNumber(), bookings.getNonInvoicedBookingsFor(guestName, departureDate));
        });
        List<RoomNumber> roomsWithoutBookings = new ArrayList<>();
        nonInvoicedBookingsForRooms.forEach(((roomNumber, bookings1) -> {
            if (bookings1.size() == 0){
                roomsWithoutBookings.add(roomNumber);
            }
        }));
        if (roomsWithoutBookings.size() > 0) {
            return Either.ofError(new Error(String.format("No bookings to be invoiced for given customer " +
                    "'%s', departureDate [%s] and roomNumbers %s", guestName.guestName(), departureDate, roomsWithoutBookings)));
        }
        Amount totalAmount =
                nonInvoicedBookingsForRooms.values().stream()
                        .map(bookingList -> bookingList.stream()
                                .map(booking -> new Amount(100.0 * booking.numberOfDays()))
                                .reduce(Amount.ZERO, Amount::add)
                        ).reduce(Amount.ZERO, Amount::add);
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

        bookings.markBookingsAsInvoiced(nonInvoicedBookingsForRooms);

        Invoice invoice = new Invoice(new InvoiceId(UUID.randomUUID().toString()), guestName, nonInvoicedBookingsForRooms, totalAmount);

        invoiceRepository.save(invoice);

        return Either.ofResult(invoice);
    }

}
