package persistence;

import java.time.LocalDate;

public record ArrivalDate(LocalDate arrivalDate) {
    public ArrivalDate(int year, int month, int day){
        this(LocalDate.of(year, month, day));
    }
    public boolean isOnOrBefore(LocalDate date) {
        return date.equals(arrivalDate) || date.isAfter(arrivalDate);
    }

    public long toEpochDay() {
        return arrivalDate.toEpochDay();
    }

    public ArrivalDate plusDays(int i) {
        return new ArrivalDate(arrivalDate.plusDays(i));
    }

    public ArrivalDate minusDays(int i) {
        return new ArrivalDate(arrivalDate.minusDays(i));
    }
}
