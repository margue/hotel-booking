package persistence;

import java.util.List;

public class Invoice {

    private GuestName guestName;
    private List<BookingsForRoom> bookingsForRooms;
    private Amount totalAmount;

    public Invoice(GuestName guestName, List<BookingsForRoom> bookingsForRooms, Amount totalAmount) {
        this.guestName = guestName;
        this.bookingsForRooms = bookingsForRooms;
        this.totalAmount = totalAmount;
    }

    public GuestName getGuestName() {
        return guestName;
    }

    public List<BookingsForRoom> getBookingsForRooms() {
        return bookingsForRooms;
    }

    public Amount getTotalAmount() {
        return totalAmount;
    }
}
