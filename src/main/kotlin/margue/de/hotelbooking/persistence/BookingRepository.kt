package margue.de.hotelbooking.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.*

interface BookingRepository : JpaRepository<Booking, UUID>
