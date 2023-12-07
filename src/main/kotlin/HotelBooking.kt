import java.time.LocalDate

// TODO: JMolecules @ValueObject
data class Booking(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    fun contains(date: LocalDate): Boolean =
        (date == startDate || date.isAfter(startDate)) && date.isBefore(endDate)
}

// TODO: JMolecules @Aggregate
data class Allotment(
    val roomCount: Int,
    // blockedIntervals
    val bookings: List<Booking>, // all bookings not checked out yet
) {
    fun bookingCount(date: LocalDate): Int =
        bookings.count { existingBooking ->
            existingBooking.contains(date)
        }
    fun roomCount(): Int = roomCount // - blocked intervals
}

enum class BookingSuccess {
    SUCCESS,
    FAILURE, // which dates are already fully booked?
}

fun bookRoom(bookingRequest: RequestBooking, allotment: Allotment): Pair<Allotment, BookingSuccess> =
    when (
        bookingRequest.toListOfDates()
            .map { allotment.bookingCount(it) }
            .map { c -> allotment.roomCount() - c }
            .filter { d -> d == 0 }
            .size
    ) {
        0 -> Pair(
            allotment.copy(
                bookings = allotment.bookings.plus(
                    Booking(
                        bookingRequest.startDate,
                        bookingRequest.endDate,
                    ),
                ),
            ),
            BookingSuccess.SUCCESS,
        )

        else -> Pair(allotment, BookingSuccess.FAILURE)
    }

// TODO: JMolecules @Command
data class RequestBooking(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    fun toListOfDates(): List<LocalDate> {
        var currentDate = startDate
        val totalDates = mutableListOf<LocalDate>()
        while (currentDate.isBefore(endDate)) {
            totalDates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return totalDates
    }
}
