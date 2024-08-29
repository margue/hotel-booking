package persistence;

import java.util.List;
import java.util.Map;

public record Invoice(InvoiceId id, GuestName guestName, List<Booking> invoicedBookings, Amount totalAmount) {
}
