package persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {

    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room){
        rooms.put(room.getRoomNumber(), room);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public List<Room> findAllRoomsWithBookingIntervalsByCustomerName(String customerName) {
        List<Room> rooms = new ArrayList<>();
        for(Room room : this.rooms.values()){
            for(BookingInterval interval : room.getBookings()){
                if(Objects.equals(interval.getCustomerName(), customerName)){
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

    // only for testing purposes
    public List<BookingInterval> findAllBookingIntervalsByCustomerName(String customerName) {
        List<BookingInterval> bookingIntervals = new ArrayList<>();
        for(Room room : rooms.values()){
            for(BookingInterval interval : room.getBookings()){
                if(Objects.equals(interval.getCustomerName(), customerName)){
                    bookingIntervals.add(interval);
                }
            }
        }
        return bookingIntervals;
    }
}
