package persistence;

import java.util.List;

public class Room {

    private String roomNumber;
    private List<BookingInterval> bookings;

    public Room(String roomNumber, List<BookingInterval> bookings) {
        this.roomNumber = roomNumber;
        this.bookings = bookings;
    }

    public String getRoomNumber(){
        return roomNumber;
    }

    public List<BookingInterval> getBookings() {
        return bookings;
    }
}
