package persistence;

import java.util.ArrayList;
import java.util.List;

public class BookingsForRoom {
    private RoomNumber roomNumber;
    private List<Booking> bookings;
    public BookingsForRoom(RoomNumber roomNumber) {
        this.roomNumber = roomNumber;
        this.bookings = new ArrayList<>();
    }

    public BookingsForRoom add(List<Booking> bookings) {
        this.bookings.addAll(bookings);
        return this;
    }

    public RoomNumber roomNumber() {
        return roomNumber;
    }

    public List<Booking> bookings() {
        return bookings;
    }
}
