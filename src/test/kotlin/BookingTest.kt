import org.assertj.core.api.Assertions
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.time.LocalDate

class BookingTest {

    @ParameterizedTest
    @MethodSource("getBookingRequest")
    fun booking_contains(date: LocalDate, expectedContains: Boolean) {
        // GIVEN
        val booking = Booking(
            startDate = LocalDate.of(2022, 10, 1),
            endDate = LocalDate.of(2022, 10, 15),
        )

        // WHEN
        val contains = booking.contains(date)

        // THEN
        Assertions.assertThat(contains).isEqualTo(expectedContains)
    }

    companion object {
        @JvmStatic
        fun getBookingRequest(): List<Arguments> = listOf(
            Arguments.of(LocalDate.of(2022, 9, 30), false),
            Arguments.of(LocalDate.of(2022, 10, 1), true),
            Arguments.of(LocalDate.of(2022, 10, 14), true),
            Arguments.of(LocalDate.of(2022, 10, 15), false),
        )
    }
}
