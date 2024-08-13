package persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BookingInterval {

    private final ArrivalDate arrivalDate;
    private final DepartureDate departureDate;
    private GuestName guestName;
    private boolean invoiced = false;
    private boolean checkedIn = false;
    private boolean checkedOut = false;

    public BookingInterval(ArrivalDate arrivalDate, DepartureDate departureDate, GuestName guestName) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.guestName = guestName;
    }

    public boolean contains(LocalDate date) {
            return arrivalDate.isOnOrBefore(date) && departureDate.isAfter(date);
    }

    public List<LocalDate> dates(){
        return arrivalDate.arrivalDate().datesUntil(departureDate.departureDate()).collect(Collectors.toList());
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
}
