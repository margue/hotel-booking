package margue.de.hotelbooking.persistence

import jakarta.persistence.*
import java.util.*

@Entity
class Room(
    @Id
    val roomNumber: String,

    @OneToMany(
        mappedBy = "room",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var bookings: MutableList<Booking> = mutableListOf(),
)
