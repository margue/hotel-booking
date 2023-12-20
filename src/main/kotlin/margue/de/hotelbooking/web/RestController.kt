package margue.de.hotelbooking.web

import margue.de.hotelbooking.domain.*
import margue.de.hotelbooking.persistence.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.time.*
import java.util.*

@RestController
class RestController(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository,
    private val bookingService: BookingService,
) {

    @GetMapping("/booking")
    fun getAllBookings(): List<Booking> {
        return bookingRepository.findAll()
    }

    @PostMapping("/booking")
    fun createBooking(@RequestBody dto: BookingDto): ResponseEntity<Unit> {
        bookingService.bookRoom(dto.guest, dto.startDate, dto.endDate)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/room")
    fun getAllRooms(): List<Room> {
        return roomRepository.findAll()
    }

    @GetMapping("/room/{roomNumber}")
    fun getRoom(@PathVariable("roomNumber") roomNumber: String): Optional<Room> {
        return roomRepository.findById(roomNumber)
    }

    @PostMapping("/room")
    fun createRoom(@RequestBody dto: RoomDto): ResponseEntity<Unit> {
        roomRepository.save(Room(dto.roomNumber))
        return ResponseEntity(HttpStatus.OK)
    }
}

data class BookingDto(
    val guest: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class RoomDto(
    val roomNumber: String
)
