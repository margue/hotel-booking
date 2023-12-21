package nicole.entity

import Booking
import java.time.LocalDate

data class RoomWithBookings (
    val roomNumber: RoomNumber, // id!
    val bookings: List<Booking>
){
    fun invariantsAreSatisfied(): Boolean =
        true // satisfied by construction as we only add bookings for this room

}

// Aggregate
data class Rooms (
    val rooms: List<RoomWithBookings>
){
    fun invariantsAreSatisfied(): Boolean =
        true // satisfied by construction as we only add bookings for the respective room

    fun bookingCount(date: LocalDate): Int =
        rooms.fold(0) { acc, room ->
            acc + room.bookings.count { existingBooking -> // auf 0 | 1 coercen?
                existingBooking.contains(date)
            }
        }

    fun roomCount(): Int = rooms.size // - blocked intervals

}

// Wir brauchen eine Aufgabenstellung, die das obere sehr begünstigt und das andere sehr erschwert!
// Feststellung muss sein: In der anderen Modellierung arbeiten wir ständig auf der Liste der Map
// -> anämisches Modell
// -> Funktionalität in die Liste -> Liste als Objekt -> RoomWithBookings
// Kann man das Schritt für Schritt motivieren?
