package persistence;

import java.time.LocalDate;
import java.util.List;

public class Booking {

    private final ArrivalDate arrivalDate;
    private final DepartureDate departureDate;
    private final GuestName guestName;
    private final RoomNumber roomNumber;
    private boolean invoiced = false;
    private boolean checkedIn = false;
    private boolean checkedOut = false;

    public Booking(ArrivalDate arrivalDate, DepartureDate departureDate, GuestName guestName, RoomNumber roomNumber) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
    }

    public boolean contains(LocalDate date) {
            return arrivalDate.isOnOrBefore(date) && departureDate.isAfter(date);
    }

    public long numberOfDays(){
        return arrivalDate.arrivalDate().datesUntil(departureDate.departureDate()).count();
    }

    public ArrivalDate getArrivalDate() {
        return arrivalDate;
    }

    public DepartureDate getDepartureDate() {
        return departureDate;
    }
    public GuestName getGuestName() {
        return guestName;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public void setInvoiced(boolean invoiced) {
        this.invoiced = invoiced;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
    }

    public boolean isCheckedOut() {
        return this.checkedOut;
    }

    public RoomNumber getRoomNumber() { return roomNumber; }

    public boolean isSameDateSameGuest(Booking b) {
        return b.getArrivalDate().equals(arrivalDate) &&
                b.getDepartureDate().equals(departureDate) &&
                b.getGuestName().equals(guestName);
    }

    public Amount getTotalAmount() {
        return new Amount(100.0 * this.numberOfDays());
    }

    public void markAsInvoiced(){
        invoiced = true;
    }
}
