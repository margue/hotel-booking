package persistence;

import java.util.List;

public class Room {

    private final RoomNumber roomNumber;
    private final BookingsForRoom bookings;

    public Room(RoomNumber roomNumber, List<Booking> bookings) {
        this.roomNumber = roomNumber;
        this.bookings = new BookingsForRoom(roomNumber).add(bookings);
    }

    public Room(RoomNumber roomNumber) {
        this.roomNumber = roomNumber;
        this.bookings = new BookingsForRoom(roomNumber);
    }

    public RoomNumber getRoomNumber() {
        return roomNumber;
    }

    public BookingsForRoom getBookingsForRoom() { return bookings; }
}
