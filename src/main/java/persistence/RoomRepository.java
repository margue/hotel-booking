package persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {

    private Map<RoomNumber, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public Map<RoomNumber, Room> getRooms() {
        return rooms;
    }

    public List<Room> findAllRoomsWithBookingIntervalsByGuestName(GuestName guestName) {
        List<Room> rooms = new ArrayList<>();
        for (Room room : this.rooms.values()) {
            for (BookingInterval interval : room.getBookings()) {
                if (Objects.equals(interval.getGuestName(), guestName)) {
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

    // only for testing purposes
    public List<BookingInterval> findAllBookingIntervalsByGuestName(GuestName guestName) {
        List<BookingInterval> bookingIntervals = new ArrayList<>();
        for (Room room : rooms.values()) {
            for (BookingInterval interval : room.getBookings()) {
                if (Objects.equals(interval.getGuestName(), guestName)) {
                    bookingIntervals.add(interval);
                }
            }
        }
        return bookingIntervals;
    }

    public void markBookingsAsInvoiced(Map<RoomNumber, List<BookingInterval>> bookingsForRooms) {
        bookingsForRooms.keySet().forEach(roomNumber -> {
            Room room = rooms.get(roomNumber);
            room.getBookings().forEach(booking -> {
                if (listContainsBooking(bookingsForRooms.get(roomNumber), booking)) {
                    booking.setInvoiced(true);
                }
            });
            save(room);
        });
    }

    private boolean listContainsBooking(List<BookingInterval> bookingIntervals, BookingInterval booking) {
        for (BookingInterval bookingInterval : bookingIntervals) {
            if (bookingInterval.getGuestName().equals(booking.getGuestName()) && bookingInterval.getStartDate().equals(booking.getStartDate())) {
                return true;
            }
        }
        return false;
    }
}
