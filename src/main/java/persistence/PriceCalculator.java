package persistence;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// Domain Service
public class PriceCalculator {

    public static Amount priceFor(long numberOfNights) {
        return new Amount(100.0 * numberOfNights);
    }

    public static Amount priceFor(Booking booking) {
        return priceFor(booking.numberOfNights());
    }

    public static Amount priceFor(List<Booking> bookingList) {
        return bookingList.stream()
                .map(PriceCalculator::priceFor)
                .reduce(Amount.ZERO, Amount::add);
    }
}
