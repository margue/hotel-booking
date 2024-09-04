package persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BookingsRepository {
    private final Map<RoomNumber, BookingsForRoom> bookingsByRoom  = new HashMap<>();

    public void save(BookingsForRoom bookings){
        bookingsByRoom.put(bookings.roomNumber(), bookings);
    }

    public Collection<BookingsForRoom> getBookingsForRooms() {
        return bookingsByRoom.values();
    }

    public BookingsForRoom getBookingsForRoom(RoomNumber roomNumber) {
        return bookingsByRoom.getOrDefault(roomNumber, new BookingsForRoom(roomNumber));
    }
}
