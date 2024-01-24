package service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.BookingInterval;
import persistence.Room;
import persistence.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HotelServiceTest {

    public HotelService setupHotelService(int numberOfRooms) {
        RoomRepository rooms = new RoomRepository();
        for (int i = 1; i <= numberOfRooms; i++) {
            rooms.save(new Room(Integer.toString(i), new ArrayList<>()));
        }
        return new HotelService(rooms);
    }


    @Test
    void requestRoom_roomAvailable() {
        // GIVEN
        HotelService service = setupHotelService(1);
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 11);

        // WHEN
        Double price = service.requestRoom(startDate, endDate);

        // THEN
        assertThat(price).isEqualTo(100.0);
    }

    @Test
    void requestRoom_roomAvailableForMultipleNights() {
        // GIVEN
        HotelService service = setupHotelService(1);
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 12);

        // WHEN
        Double price = service.requestRoom(startDate, endDate);

        // THEN
        assertThat(price).isEqualTo(200.0);
    }

    @Test
    void requestRoom_roomNotAvailable() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 12);
        RoomRepository rooms = new RoomRepository();
        List<BookingInterval> bookings = new ArrayList<>();
        bookings.add(new BookingInterval(startDate, endDate));
        rooms.save(new Room("1", bookings));
        HotelService service = new HotelService(rooms);

        // WHEN
        Double price = service.requestRoom(startDate, endDate);

        // THEN
        assertThat(price).isNull();
    }

    @Test
    void requestRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 11);
        RoomRepository rooms = new RoomRepository();
        List<BookingInterval> bookings = new ArrayList<>();
        bookings.add(new BookingInterval(startDate.plusDays(5), endDate.plusDays(7)));
        rooms.save(new Room("1", bookings));
        HotelService service = new HotelService(rooms);

        // WHEN
        Double price = service.requestRoom(startDate, endDate);

        // THEN
        assertThat(price).isEqualTo(100.0);
    }
}
