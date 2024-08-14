package persistence;

import java.time.LocalDate;

public record DepartureDate(LocalDate departureDate) {
    public DepartureDate(int year, int month, int day){
        this(LocalDate.of(year, month, day));
    }

    public boolean isAfter(LocalDate date) {
        return departureDate.isAfter(date);
    }

    public DepartureDate plusDays(int i) {
        return new DepartureDate(departureDate.plusDays(i));
    }

    public boolean isOnOrBefore(DepartureDate otherDate) {
        return !otherDate.isAfter(departureDate);

        // A.isOnOrBefore B
        // A <= B
        // !(B > A) --> B <= A --> A >= B

        // return departureDate.equals(otherDate.departureDate) || departureDate.isBefore(otherDate.departureDate);
    }

    public DepartureDate minusDays(int i) {
        return new DepartureDate(departureDate.minusDays(i));
    }
}
