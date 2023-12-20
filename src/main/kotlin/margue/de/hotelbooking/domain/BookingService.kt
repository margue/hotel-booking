package margue.de.hotelbooking.domain

import margue.de.hotelbooking.persistence.*
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BookingService(
    private val roomRepository: RoomRepository,
) {
    fun bookRoom(guest: String, startDate: LocalDate, endDate: LocalDate) {
        val availableRoom = findFreeRoomsFor(startDate, endDate).first()
        availableRoom.let { room ->
            room.bookings.add(Booking(guest = guest, startDate = startDate, endDate = endDate, room = room))
            roomRepository.save(availableRoom)
        }
    }

    private fun findFreeRoomsFor(startDate: LocalDate, endDate: LocalDate): List<Room> {
        return roomRepository.findAll().filter {
            it.bookings.none { existingBooking ->
                (
                    (
                        startDate.isEqual(existingBooking.startDate) ||
                            startDate.isAfter(existingBooking.startDate)
                        ) &&
                        startDate.isBefore(existingBooking.endDate)
                    ) ||
                    (
                        endDate.isAfter(existingBooking.startDate) &&
                            endDate.isBefore(existingBooking.endDate)
                        )
            }
        }.ifEmpty {
            throw IllegalArgumentException(
                "No room free for requested period [startdate: $startDate, enddate: $endDate]",
            )
        }
    }
}
