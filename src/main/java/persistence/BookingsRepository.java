package persistence;

import java.util.HashMap;
import java.util.Map;

public class BookingsRepository {
    private final Map<RoomNumber, BookingsForRoom> bookingsByRoom  = new HashMap<>();

    public void save(BookingsForRoom bookings){
        bookingsByRoom.put(bookings.roomNumber(), bookings);
    }

}
