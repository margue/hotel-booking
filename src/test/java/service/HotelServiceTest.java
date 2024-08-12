package service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class HotelServiceTest {

    RoomNumber roomNumber1 = new RoomNumber("1");
    RoomNumber roomNumber2 = new RoomNumber("2");

    public HotelService setupHotelService(int numberOfRooms) {
        RoomRepository rooms = new RoomRepository();
        for (int i = 1; i <= numberOfRooms; i++) {
            rooms.save(new Room(new RoomNumber(Integer.toString(i)), new ArrayList<>()));
        }
        return new HotelService(rooms);
    }

    public RoomRepository setupRoomsWithOneRoomAndBookings(BookingInterval... bookingIntervals){
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room(new RoomNumber("1"), new ArrayList<>(Arrays.asList(bookingIntervals))));
        return rooms;
    }

    @Test
    void requestRoom_roomAvailable() {
        // GIVEN
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Double price = service.requestRoom(arrivalDate, endDate);

        // THEN
        assertThat(price).isEqualTo(100.0);
    }

    @Test
    void requestRoom_roomAvailableForMultipleNights() {
        // GIVEN
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);

        // WHEN
        Double price = service.requestRoom(arrivalDate, endDate);

        // THEN
        assertThat(price).isEqualTo(200.0);
    }

    @Test
    void requestRoom_roomNotAvailable() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate)));

        // WHEN
        Double price = service.requestRoom(arrivalDate, endDate);

        // THEN
        assertThat(price).isNull();
    }

    @Test
    void requestRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate.plusDays(5), endDate.plusDays(7))));

        // WHEN
        Double price = service.requestRoom(arrivalDate, endDate);

        // THEN
        assertThat(price).isEqualTo(100.0);
    }

    @Test
    void bookRoom_bookingRequiresGuestName() {
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Throwable t = catchThrowable(() -> service.bookRoom(arrivalDate, endDate, new GuestName(null)));

        // THEN
        assertThat(t).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bookRoom_roomAvailable() {
        // GIVEN
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);

        // WHEN
        service.bookRoom(arrivalDate, endDate, new GuestName("Peter"));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByGuestName(new GuestName("Peter"));
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundIntervals.getFirst().getDepartureDate()).isEqualTo(endDate);
    }

    @Test
    void bookRoom_bookTwoRoomsForSameNights() {
        // GIVEN
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room(roomNumber1, new ArrayList<>()));
        rooms.save(new Room(roomNumber2, new ArrayList<>()));
        HotelService service = new HotelService(rooms);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);

        // WHEN
        service.bookRoom(arrivalDate, endDate, new GuestName("Peter"));
        service.bookRoom(arrivalDate, endDate, new GuestName("Peter"));

        // THEN
        List<Room> foundRooms = rooms.findAllRoomsWithBookingIntervalsByGuestName(new GuestName("Peter"));
        assertThat(foundRooms).hasSize(2);
        assertThat(foundRooms).extracting("roomNumber")
                        .containsExactly(roomNumber1, roomNumber2);
        assertThat(foundRooms.get(0).getBookings().get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundRooms.get(0).getBookings().get(0).getDepartureDate()).isEqualTo(endDate);
        assertThat(foundRooms.get(1).getBookings().get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundRooms.get(1).getBookings().get(0).getDepartureDate()).isEqualTo(endDate);
    }

    @Test
    void bookRoom_roomAvailableForMultipleNights() {
        // GIVEN
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);

        // WHEN
        service.bookRoom(arrivalDate, endDate, new GuestName("Fred"));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByGuestName(new GuestName("Fred"));
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundIntervals.getFirst().getDepartureDate()).isEqualTo(endDate);
    }

    @Test
    void bookRoom_roomNotAvailable() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate));
        HotelService service = new HotelService(rooms);

        // WHEN
        Throwable t = catchThrowable(() -> service.bookRoom(arrivalDate, endDate, new GuestName("Jack")));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByGuestName(new GuestName("Jack"));
        assertThat(foundIntervals).hasSize(0);
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bookRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 11);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate.plusDays(5), endDate.plusDays(7)));
        HotelService service = new HotelService(rooms);

        // WHEN
        service.bookRoom(arrivalDate, endDate, new GuestName("Jim"));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByGuestName(new GuestName("Jim"));
        assertThat(foundIntervals).hasSize(1);
        assertThat(foundIntervals.getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundIntervals.getFirst().getDepartureDate()).isEqualTo(endDate);
    }

    @Test
    void checkIn_roomWasBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms);

        // WHEN
        List<RoomNumber> checkedInRoomNumbers = service.checkIn(new GuestName("Fritz"), arrivalDate);

        // THEN
        assertThat(checkedInRoomNumbers.size()).isEqualTo(1);
        assertThat(checkedInRoomNumbers.getFirst().number()).isEqualTo("1");
    }

    @Test
    void checkIn_roomWasNotBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(rooms);

        // WHEN
        Throwable t = catchThrowable(() -> service.checkIn(new GuestName("Fritz"), arrivalDate));

        // THEN
        List<BookingInterval> foundIntervals = rooms.findAllBookingIntervalsByGuestName(new GuestName("Fritz"));
        assertThat(foundIntervals).hasSize(0);
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkIn_roomWasBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate1 = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate1,
                endDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms);
        ArrivalDate arrivalDate2 = arrivalDate1.plusDays(17);

        // WHEN
        List<RoomNumber> checkedInRoomNumbers = service.checkIn(new GuestName("Fritz"), arrivalDate2);

        // THEN
        assertThat(checkedInRoomNumbers.size()).isEqualTo(0);
    }

    @Test
    void checkOut_roomWasBooked_error() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms);

        // WHEN
        Throwable t = catchThrowable(() -> service.checkOut(new GuestName("Fritz"), roomNumber1, endDate));

        // THEN
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkOut_roomWasCheckedIn_error() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms);
        service.checkIn(new GuestName("Fritz"), arrivalDate);

        // WHEN
        Throwable t = catchThrowable(() -> service.checkOut(new GuestName("Fritz"), roomNumber1, endDate));

        // THEN
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkOut_roomWasInvoiced() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate endDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new BookingInterval(arrivalDate,
                endDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms);
        service.checkIn(new GuestName("Fritz"), arrivalDate);

        PaymentRepository paymentRepository = new PaymentRepository();
        PaymentService paymentService = new PaymentService(paymentRepository, rooms);
        paymentService.payAmount(new GuestName("Fritz"), new Amount(200.0));
        paymentService.produceInvoice(new GuestName("Fritz"), endDate, Collections.singletonList(roomNumber1));

        // WHEN
        service.checkOut(new GuestName("Fritz"), roomNumber1, endDate);

        // THEN
        Assertions.assertThat(rooms.getRooms().get(roomNumber1).getBookings().getFirst().isCheckedOut()).isTrue();
    }

}
