package margue.de.hotelbooking.persistence

import org.springframework.data.jpa.repository.*
import java.time.*

interface RoomRepository : JpaRepository<Room, String>
