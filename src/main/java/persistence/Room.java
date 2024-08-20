package persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Room {

    private final RoomNumber roomNumber;
    private final List<Booking> bookings;

    public Room(RoomNumber roomNumber, List<Booking> bookings) {
        this.roomNumber = roomNumber;
        this.bookings = bookings;
    }

    public List<Booking> getNonInvoicedBookingsFor(GuestName guestName, DepartureDate departureDate) {
        return getBookings().stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> departureDate.isOnOrBefore(booking.getDepartureDate()))
                .filter(booking -> !booking.isInvoiced())
                .filter(Booking::isCheckedIn).collect(Collectors.toList());
    }

    public List<Booking> getBookingsUntil(GuestName guestName, DepartureDate departureDate) {
        return getBookings().stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> booking.getDepartureDate().equals(departureDate))
                .toList();
    }

    public List<Booking> getBookingsFrom(GuestName guestName, ArrivalDate arrivalDate) {
        return getBookings().stream()
                .filter(booking -> booking.getGuestName().equals(guestName))
                .filter(booking -> booking.getArrivalDate().equals(arrivalDate))
                .toList();
    }

    public RoomNumber getRoomNumber() {
        return roomNumber;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    private boolean dateIsFree(LocalDate date) {
        for (Booking booking : bookings) {
            if (booking.contains(date)) {
                return false;
            }
        }
        return true;
    }

    public boolean roomIsFree(ArrivalDate arrivalDate, DepartureDate departureDate) {
        for (LocalDate date : arrivalDate.arrivalDate().datesUntil(departureDate.departureDate()).toList()) {
            if (!dateIsFree(date)) {
                return false;
            }
        }
        return true;
    }
}
