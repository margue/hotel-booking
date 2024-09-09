package persistence;

import java.util.*;

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

    public List<BookingsForRoom> getBookingsForRooms(List<RoomNumber> roomNumbers) {
        return roomNumbers.stream().map(this::getBookingsForRoom).toList();
    }

    public List<BookingsForRoom> findBookingsForRoomsWithBookingFor(GuestName guestName, ArrivalDate arrivalDate) {
        return bookingsByRoom.values().stream().filter(bookings -> ! bookings.getBookingsFrom(guestName, arrivalDate).isEmpty()).toList();
    }

    public List<BookingsForRoom> findBookingsForRoomsWithBookingFor(GuestName guestName) {
        return bookingsByRoom.values().stream().filter(bookings -> ! bookings.getBookingsFor(guestName).isEmpty()).toList();
    }

    public void markBookingsAsInvoiced(Map<RoomNumber, List<Booking>> bookingsBeingInvoiced) {
        bookingsBeingInvoiced.forEach((roomNumber, bookingsBeingInvoicedForOneRoom) -> {
            BookingsForRoom existingBookings = bookingsByRoom.get(roomNumber);
            existingBookings.bookings().forEach(booking -> {
                if (listContainsBooking(bookingsBeingInvoicedForOneRoom, booking)) {
                    booking.setInvoiced(true);
                }
            });
            save(existingBookings);
        });
    }
    private boolean listContainsBooking(List<Booking> bookings, Booking booking) {
        for (Booking aBooking : bookings) {
            if (aBooking.getGuestName().equals(booking.getGuestName()) && aBooking.getArrivalDate().equals(booking.getArrivalDate())) {
                return true;
            }
        }
        return false;
    }
}
