package persistence;

import java.util.List;

public class Room {

    private final BookingsForRoom bookings;

    public Room(BookingsForRoom bookings) {
        this.bookings = bookings;
    }

    public Room(RoomNumber roomNumber, List<Booking> bookings) {
        this.bookings = new BookingsForRoom(roomNumber).add(bookings);
    }

    public RoomNumber getRoomNumber() {
        return bookings.roomNumber();
    }

    public List<Booking> getBookings() {
        return bookings.bookings();
    }

    public BookingsForRoom getBookingsForRoom() { return bookings; }
}
