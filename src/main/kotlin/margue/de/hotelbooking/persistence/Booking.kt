package margue.de.hotelbooking.persistence

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.*
import java.util.*

@Entity
class Booking(
    @Id
    var id: UUID = UUID.randomUUID(),
    val guest: String,
    val startDate: LocalDate,
    val endDate: LocalDate,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    val room: Room,
)
