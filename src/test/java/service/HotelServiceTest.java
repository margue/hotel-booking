package service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import persistence.BookingInterval;
import persistence.Room;
import persistence.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class HotelServiceTest {

    public HotelService setupHotelService(int numberOfRooms) {
        RoomRepository rooms = new RoomRepository();
        for (int i = 1; i <= numberOfRooms; i++) {
            rooms.save(new Room(Integer.toString(i), new ArrayList<>()));
        }
        return new HotelService(rooms);
    }

    public RoomRepository setupRoomsWithOneRoomAndBookings(BookingInterval... bookingIntervals){
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room("1", new ArrayList<>(Arrays.asList(bookingIntervals))));
        return rooms;
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
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new BookingInterval(startDate,
                endDate)));

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
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new BookingInterval(startDate.plusDays(5), endDate.plusDays(7))));

        // WHEN
        Double price = service.requestRoom(startDate, endDate);

        // THEN
        assertThat(price).isEqualTo(100.0);
    }

    @Test
    void bookRoom_bookingRequiresCustomerName() {
        HotelService service = setupHotelService(1);
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 11);

        // WHEN
        Throwable t = catchThrowable(() -> service.bookRoom(startDate, endDate, null));

        // THEN
        assertThat(t).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bookRoom_roomAvailable() {
        // GIVEN
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 11);

        // WHEN
        service.bookRoom(startDate, endDate, "Peter");

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Peter");
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(foundIntervals.get(0).getEndDate()).isEqualTo(endDate);
    }

    @Test
    void bookRoom_roomAvailableForMultipleNights() {
        // GIVEN
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 12);

        // WHEN
        service.bookRoom(startDate, endDate, "Fred");

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Fred");
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(foundIntervals.get(0).getEndDate()).isEqualTo(endDate);
    }

    @Test
    void bookRoom_roomNotAvailable() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(startDate,
                endDate));
        HotelService service = new HotelService(rooms);

        // WHEN
        Throwable t = catchThrowable(() -> service.bookRoom(startDate, endDate, "Jack"));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Jack");
        assertThat(foundIntervals).hasSize(0);
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bookRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 11);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(startDate.plusDays(5), endDate.plusDays(7)));
        HotelService service = new HotelService(rooms);

        // WHEN
        service.bookRoom(startDate, endDate, "Jim");

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Jim");
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(foundIntervals.get(0).getEndDate()).isEqualTo(endDate);
    }

    @Test
    void checkIn_roomWasBooked() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        LocalDate endDate = LocalDate.of(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(startDate,
                endDate, "Fritz"));
        HotelService service = new HotelService(rooms);

        // WHEN
        service.checkIn("Fritz", startDate);

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Fritz");
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(foundIntervals.get(0).getEndDate()).isEqualTo(endDate);
        assertThat(foundIntervals.get(0).getIsCheckedIn()).isTrue();
    }

    @Test
    void checkIn_roomWasNotBooked() {
        // GIVEN
        LocalDate startDate = LocalDate.of(2020, 10, 10);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);

        // WHEN
        Throwable t = catchThrowable(() -> service.checkIn("Fritz", startDate));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByCustomerName("Fritz");
        assertThat(foundIntervals).hasSize(0);
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

}
