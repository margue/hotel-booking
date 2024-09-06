package persistence;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {

    private final Map<RoomNumber, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public Map<RoomNumber, Room> getRooms() {
        return rooms;
    }

    public List<Room> findAllRoomsWithBookingsByGuestName(GuestName guestName) {
        Set<Room> rooms = new HashSet<>();
        for (Room room : this.rooms.values()) {
            for (Booking booking : room.getBookings()) {
                if (Objects.equals(booking.getGuestName(), guestName)) {
                    rooms.add(room);
                }
            }
        }
        return rooms.stream().toList();
    }

    // only for testing purposes
    public List<Booking> findAllBookingsByGuestName(GuestName guestName) {
        List<Booking> bookings = new ArrayList<>();
        for (Room room : rooms.values()) {
            for (Booking booking : room.getBookings()) {
                if (Objects.equals(booking.getGuestName(), guestName)) {
                    bookings.add(booking);
                }
            }
        }
        return bookings;
    }

}
