package persistence;

import java.time.LocalDate;

public class BookingInterval {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public BookingInterval(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
