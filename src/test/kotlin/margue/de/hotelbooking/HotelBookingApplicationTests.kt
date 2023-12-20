package margue.de.hotelbooking

import com.fasterxml.jackson.databind.*
import margue.de.hotelbooking.web.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.*

@SpringBootTest
@AutoConfigureMockMvc
class HotelBookingApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun contextLoads() {
    }

    @Test
    fun bookRooms() {
        bookARoom()
    }

    private fun bookARoom() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/booking")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        BookingDto(
                            guest = "Martin",
                            startDate = LocalDate.of(2023, 12, 20),
                            endDate = LocalDate.of(2023, 12, 23),
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
    }
}
