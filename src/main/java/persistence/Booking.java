package persistence;

import java.time.LocalDate;

// Value Object
public class Booking {

    private final ArrivalDate arrivalDate;
    private final DepartureDate departureDate;
    private final GuestName guestName;
    private final boolean invoiced;
    private final boolean checkedIn;
    private boolean checkedOut;

    public Booking(ArrivalDate arrivalDate, DepartureDate departureDate, GuestName guestName) {
        this(arrivalDate, departureDate, guestName, false, false, false);
    }

    private Booking(ArrivalDate arrivalDate, DepartureDate departureDate, GuestName guestName, boolean invoiced, boolean checkedIn, boolean checkedOut) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.guestName = guestName;
        this.invoiced = invoiced;
        this.checkedIn = checkedIn;
        this.checkedOut = checkedOut;
    }

    public boolean contains(LocalDate date) {
            return arrivalDate.isOnOrBefore(date) && departureDate.isAfter(date);
    }

    public long numberOfNights(){
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

    public Booking checkIn() {
        return new Booking(this.arrivalDate, this.departureDate, this.guestName, this.invoiced, true, this.checkedOut);
    }

    public Booking markAsInvoiced() {
        return new Booking(this.arrivalDate, this.departureDate, this.guestName, true, this.checkedIn, this.checkedOut);
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public Booking checkOut() {
        this.checkedOut = true;
        return this;
        // TODO return new Booking(this.arrivalDate, this.departureDate, this.guestName, this.invoiced, this.checkedIn, true);
    }

    public boolean isCheckedOut() {
        return this.checkedOut;
    }
}
