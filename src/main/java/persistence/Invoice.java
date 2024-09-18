package persistence;

import java.util.List;
import java.util.Map;

// Entity
public record Invoice(InvoiceId id, GuestName guestName, List<String> positions, Amount totalAmount) {
}
