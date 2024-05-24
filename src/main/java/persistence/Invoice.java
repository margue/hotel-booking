package persistence;

import java.util.List;
import java.util.Map;

public class Invoice {

    private GuestName guestName;
    private Map<RoomNumber, List<BookingInterval>> bookingsForRooms;
    private double totalAmount;

    public Invoice(GuestName guestName, Map<RoomNumber, List<BookingInterval>> bookingsForRooms, double totalAmount) {
        this.guestName = guestName;
        this.bookingsForRooms = bookingsForRooms;
        this.totalAmount = totalAmount;
    }

    public GuestName getGuestName() {
        return guestName;
    }

    public Map<RoomNumber, List<BookingInterval>> getBookingsForRooms() {
        return bookingsForRooms;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
