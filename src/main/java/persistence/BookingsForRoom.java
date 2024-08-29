package persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingsForRoom {
    private final RoomNumber roomNumber;
    private List<Booking> bookings;
    public BookingsForRoom(RoomNumber roomNumber) {
        this.roomNumber = roomNumber;
        this.bookings = new ArrayList<>();
    }

    public BookingsForRoom add(List<Booking> bookings) {
        this.bookings.addAll(bookings.stream().filter(b -> b.getRoomNumber().equals(this.roomNumber)).toList());
        return this;
    }

    public BookingsForRoom add(Booking booking) {
        if(booking.getRoomNumber().equals(this.roomNumber)) {
            this.bookings.add(booking);
        }
        return this;
    }

    public RoomNumber roomNumber() {
        return roomNumber;
    }

    public List<Booking> bookings() {
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

    // Error handling?
    public void addOrUpdate(Booking booking) {
        if(booking.getRoomNumber().equals(this.roomNumber)){
            if(bookings.stream().anyMatch(booking::isSameDateSameGuest)){
                bookings = bookings.stream().map(b -> b.isSameDateSameGuest(booking) ? booking : b)
                        .toList();
            } else {
                bookings.add(booking);
            }
        }
    }
}
