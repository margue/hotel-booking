package nicole.entity

import java.time.LocalDate
import java.util.UUID

data class BookingId(
    val id: UUID
){
    fun next(): BookingId = BookingId(UUID.randomUUID())
}

data class RoomNumber(
    val number: Int
)

// TODO: JMolecules @ValueObject
data class BookingForRoom(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val roomNumber: RoomNumber,
) {
    fun contains(date: LocalDate): Boolean =
        (date == startDate || date.isAfter(startDate)) && date.isBefore(endDate)
}

// TODO: JMolecules @Aggregate
data class Bookings(
    val roomCount: Int,
    // blockedIntervals
    val bookings: Map<RoomNumber, List<BookingForRoom>>, // all bookings not checked out yet
) {
    fun invariantsAreSatisfied(): Boolean =
        bookings.map {
                (number, list) -> list.filter { booking -> booking.roomNumber != number }.isEmpty()
        }.filter { isEmpty -> isEmpty == false }.isEmpty()

    fun bookingCount(date: LocalDate): Int =
        bookings.values.fold(0) { acc, list ->
            acc + list.count { existingBooking ->
                existingBooking.contains(date)
            }
        }


    fun roomCount(): Int = roomCount // - blocked intervals
}

enum class BookingSuccess {
    SUCCESS,
    FAILURE, // which dates are already fully booked?
}

fun bookArbitraryRoom(bookingRequest: RequestBooking, allotment: Bookings): Pair<Bookings, BookingSuccess> =
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
                    BookingForRoom(
                        bookingRequest.startDate,
                        bookingRequest.endDate,
                    ),
                ),
            ),
            BookingSuccess.SUCCESS,
        )

        else -> Pair(allotment, BookingSuccess.FAILURE)
    }

fun bookSpecificRoom(bookingRequest: RequestBooking, roomNumber: RoomNumber, allotment: Bookings): Pair<Bookings, BookingSuccess> =
    Pair(allotment, BookingSuccess.FAILURE)


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
