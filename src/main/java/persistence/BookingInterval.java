package persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BookingInterval {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private String customerName;

    public BookingInterval(LocalDate startDate, LocalDate endDate) {
        this(startDate, endDate, null);
    }

    public BookingInterval(LocalDate startDate, LocalDate endDate, String customerName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerName = customerName;
    }

    public boolean contains(LocalDate date) {
            return (date == startDate || date.isAfter(startDate)) && date.isBefore(endDate);
    }

    // method courtesy of Java 9 :)
    private Stream<LocalDate> datesFromTo(LocalDate startInclusive, LocalDate endExclusive) {
        long end = endExclusive.toEpochDay();
        long start = startInclusive.toEpochDay();
        if (end < start) {
            throw new IllegalArgumentException(endExclusive + " < " + this);
        }
        return LongStream.range(start, end).mapToObj(LocalDate::ofEpochDay);
    }


    public List<LocalDate> dates(){
        return datesFromTo(startDate, endDate).collect(Collectors.toList());
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public String getCustomerName() {
        return customerName;
    }
}
