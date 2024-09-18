package service;

import persistence.*;

import java.util.*;

public class PaymentService {

    private final BookingsRepository bookings;
    private final InvoiceRepository invoiceRepository;
    private final CheckingAccountRepository checkingAccounts;

    public PaymentService(CheckingAccountRepository checkingAccounts) {
        this.invoiceRepository = new InvoiceRepository();
        this.bookings = new BookingsRepository();
        this.checkingAccounts = checkingAccounts;
    }

    public PaymentService(BookingsRepository bookings, InvoiceRepository invoiceRepository, CheckingAccountRepository checkingAccounts) {
        this.invoiceRepository = invoiceRepository;
        this.bookings = bookings;
        this.checkingAccounts = checkingAccounts;
    }

    /*
    Postcondition: Guest has paid a certain amount to the hotel.
     */
    public void payAmount(GuestName guestName, Amount amount){
        CheckingAccount account = checkingAccounts.load(guestName);
        account.addPayment(new Transaction(amount));
        checkingAccounts.save(account);
    }

    public Amount remainingCredit(GuestName guestName){
        return checkingAccounts.load(guestName).credit();
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
        nonInvoicedBookingsForRooms.forEach(((roomNumber, bookings) -> {
            if (bookings.size() == 0){
                roomsWithoutBookings.add(roomNumber);
            }
        }));
        if (roomsWithoutBookings.size() > 0) {
            return Either.ofError(new Error(String.format("No bookings to be invoiced for given customer " +
                    "'%s', departureDate [%s] and roomNumbers %s", guestName.guestName(), departureDate, roomsWithoutBookings)));
        }
        Amount totalAmount =
                nonInvoicedBookingsForRooms.values().stream()
                        .map(PriceCalculator::priceFor).reduce(Amount.ZERO, Amount::add);
        CheckingAccount account = checkingAccounts.load(guestName);
        Amount credit = account.credit();
        if(totalAmount.isMoreThan(credit)){
            return Either.ofError(new Error("Payment insufficient. Necessary payment: " + (totalAmount.subtract(credit))));
        }
        account.reduceCreditBy(totalAmount);
        checkingAccounts.save(account);

        bookings.markBookingsAsInvoiced(nonInvoicedBookingsForRooms);

        Invoice invoice = new Invoice(new InvoiceId(UUID.randomUUID().toString()), guestName, InvoiceTextGenerator.textForInvoice(nonInvoicedBookingsForRooms), totalAmount);

        invoiceRepository.save(invoice);

        return Either.ofResult(invoice);
    }

}
