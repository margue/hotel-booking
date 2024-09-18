package persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Entity
public class BookingsForRoom {
    private final RoomNumber roomNumber;
    private final List<Booking> bookings;

    public BookingsForRoom(RoomNumber roomNumber) {
        this.roomNumber = roomNumber;
        this.bookings = new ArrayList<>();
    }

    public void markBookingsAsCheckedIn(GuestName guestName, ArrivalDate arrivalDate) {
        this.bookings.replaceAll(booking ->
                booking.getGuestName().equals(guestName) && booking.getArrivalDate().equals(arrivalDate)
                        ? booking.checkIn()
                        : booking);
    }

    public BookingsForRoom add(List<Booking> bookings) {
        this.bookings.addAll(bookings);
        return this;
    }

    public BookingsForRoom add(Booking booking) {
        this.bookings.add(booking);
        return this;
    }

    public RoomNumber roomNumber() {
        return roomNumber;
    }

    public List<Booking> bookings() {
        return bookings;
    }

    public List<Booking> getNonInvoicedBookingsFor(GuestName guestName, DepartureDate departureDate) {
        return bookings.stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> departureDate.isOnOrBefore(booking.getDepartureDate()))
                .filter(booking -> !booking.isInvoiced())
                .filter(Booking::isCheckedIn).collect(Collectors.toList());
    }

    public List<Booking> getBookingsUntil(GuestName guestName, DepartureDate departureDate) {
        return bookings.stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> booking.getDepartureDate().equals(departureDate))
                .toList();
    }

    boolean dateIsFree(LocalDate date) {
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

    public List<Booking> getBookingsFrom(GuestName guestName, ArrivalDate arrivalDate) {
        return bookings.stream()
                .filter(booking -> booking.getGuestName().equals(guestName))
                .filter(booking -> booking.getArrivalDate().equals(arrivalDate))
                .toList();
    }

    public List<Booking> getBookingsFor(GuestName guestName) {
        return bookings.stream()
                .filter(booking -> booking.getGuestName().equals(guestName))
                .toList();
    }
}
