package persistence;

import java.util.*;
import java.util.stream.Collectors;

public class BookingsRepository {
    private final Map<RoomNumber, BookingsForRoom> bookingsByRoom  = new HashMap<>();

    public BookingsForRoom bookingsFor(RoomNumber roomNumber){
        return bookingsByRoom.getOrDefault(roomNumber, new BookingsForRoom(roomNumber));
    }

    public void save(BookingsForRoom bookings){
        bookingsByRoom.put(bookings.roomNumber(), bookings);
    }

    private List<Booking> allBookings(){
        return bookingsByRoom.values().stream()
                .map(BookingsForRoom::bookings)
                .flatMap(List::stream)
                .toList();
    }

    public List<Booking> getNonInvoicedBookingsFor(GuestName guestName, DepartureDate departureDate) {
        return allBookings().stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> departureDate.isOnOrBefore(booking.getDepartureDate()))
                .filter(booking -> !booking.isInvoiced())
                .filter(Booking::isCheckedIn).collect(Collectors.toList());
    }

    public List<Booking> getBookingsUntil(GuestName guestName, DepartureDate departureDate) {
        return allBookings().stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> booking.getDepartureDate().equals(departureDate))
                .toList();
    }

    public List<Booking> getBookingsFrom(GuestName guestName, ArrivalDate arrivalDate) {
        return allBookings().stream()
                .filter(booking -> booking.getGuestName().equals(guestName))
                .filter(booking -> booking.getArrivalDate().equals(arrivalDate))
                .toList();
    }

    // only for testing purposes
    public List<Booking> findAllBookingsByGuestName(GuestName guestName) {
        List<Booking> bookings = new ArrayList<>();
        for (Booking booking : this.allBookings()) {
            if (Objects.equals(booking.getGuestName(), guestName)) {
                bookings.add(booking);
            }
        }
        return bookings;
    }

    public void update(Booking booking) {
        bookingsByRoom.get(booking.getRoomNumber()).addOrUpdate(booking);
    }

    public void updateAll(List<Booking> bookings) {
        bookings.forEach(this::update);
    }
}
