package service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class HotelServiceTest {

    final RoomNumber roomNumber1 = new RoomNumber("1");
    final RoomNumber roomNumber2 = new RoomNumber("2");
    final GuestName guestWithBooking = new GuestName("Peter");

    public HotelService setupHotelService(int numberOfRooms) {
        RoomRepository rooms = new RoomRepository();
        for (int i = 1; i <= numberOfRooms; i++) {
            rooms.save(new Room(new RoomNumber(Integer.toString(i))));
        }
        return new HotelService(rooms, new BookingsRepository());
    }

    public RoomRepository setupRoomsWithNumbers(RoomNumber... roomNumbers){
        RoomRepository rooms = new RoomRepository();
        Arrays.stream(roomNumbers).toList().forEach(number -> {
            rooms.save(new Room(number));
        });
        return rooms;
    }

    public static BookingsRepository setupBookingsRepoWithBookings(Booking... bookings) {
        BookingsRepository bookingsRepository = new BookingsRepository();
        Arrays.stream(bookings).toList().forEach(booking -> {
            BookingsForRoom bookingsForRoom = new BookingsForRoom(booking.getRoomNumber()).add(booking);
            bookingsRepository.save(bookingsForRoom);
        });
        return bookingsRepository;
    }



    @Test
    void requestRoom_roomAvailable() {
        // GIVEN
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Either<Error, Amount> result = service.requestRoom(arrivalDate, departureDate);

        // THEN
        assertThat(result.isError()).isFalse();
        assertThat(result.result()).isEqualTo(new Amount(100.0));
    }

    @Test
    void requestRoom_roomAvailableForMultipleNights() {
        // GIVEN
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);

        // WHEN
        Either<Error, Amount> result = service.requestRoom(arrivalDate, departureDate);

        // THEN
        assertThat(result.isError()).isFalse();
        assertThat(result.result()).isEqualTo(new Amount(200.0));
    }

    @Test
    void requestRoom_roomNotAvailable() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        Booking booking = new Booking(arrivalDate,
                departureDate, guestWithBooking, roomNumber1);
        BookingsRepository bookings = setupBookingsRepoWithBookings(booking);
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, Amount> result = service.requestRoom(arrivalDate, departureDate);

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("No available room found for the desired dates");
    }

    @Test
    void requestRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);
        HotelService service = new HotelService(
                setupRoomsWithNumbers(roomNumber1),
                setupBookingsRepoWithBookings(new Booking(arrivalDate.plusDays(5), departureDate.plusDays(7), guestWithBooking, roomNumber1)));

        // WHEN
        Either<Error, Amount> result = service.requestRoom(arrivalDate, departureDate);

        // THEN
        assertThat(result.isError()).isFalse();
        assertThat(result.result()).isEqualTo(new Amount(100.0));
    }

    @Test
    void bookRoom_bookingRequiresGuestName() {
        HotelService service = setupHotelService(1);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName(null));

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("Guest name must not be null");
    }

    @Test
    void bookRoom_roomAvailable() {
        // GIVEN
        BookingsRepository bookings = new BookingsRepository();
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Peter"));

        // THEN
        assertThat(result.isError()).isFalse();
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Peter"));
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.get(0).getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_bookTwoRoomsForSameNights() {
        // GIVEN
        BookingsRepository bookings = new BookingsRepository();
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room(roomNumber1));
        rooms.save(new Room(roomNumber2));
        HotelService service = new HotelService(rooms, bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        Either<Error, RoomNumber> result1 = service.bookRoom(arrivalDate, departureDate, new GuestName("Peter"));
        assertThat(result1.isError()).isFalse();

        // WHEN
        Either<Error, RoomNumber> result2 = service.bookRoom(arrivalDate, departureDate, new GuestName("Peter"));

        // THEN
        assertThat(result2.isError()).isFalse();
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Peter"));
        assertThat(foundBookings).hasSize(2);
        assertThat(foundBookings).extracting("roomNumber")
                        .containsExactlyInAnyOrder(roomNumber1, roomNumber2);
        assertThat(foundBookings.get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.get(0).getDepartureDate()).isEqualTo(departureDate);
        assertThat(foundBookings.get(1).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.get(1).getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_roomAvailableForMultipleNights() {
        // GIVEN
        BookingsRepository bookings = new BookingsRepository();
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Fred"));

        // THEN
        assertThat(result.isError()).isFalse();
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Fred"));
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.get(0).getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_roomNotAvailable() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate, departureDate, guestWithBooking, roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Jack"));

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("No rooms available on the given date(s)");
        // no accidental changes to rooms:
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Jack"));
        assertThat(foundBookings).hasSize(0);
    }

    @Test
    void bookRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate.plusDays(5), departureDate.plusDays(7), guestWithBooking, roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Jim"));

        // THEN
        assertThat(result.isError()).isFalse();
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Jim"));
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.get(0).getDepartureDate()).isEqualTo(departureDate);
        // do some checks with the room?
    }

    @Test
    void checkIn_roomWasBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz"), roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate);

        // THEN
        assertThat(result.isError()).isFalse();
        assertThat(result.result().size()).isEqualTo(1);
        assertThat(result.result().get(0).number()).isEqualTo("1");
    }

    @Test
    void checkIn_roomWasNotBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        BookingsRepository bookings = new BookingsRepository();
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate);

        // THEN
        assertThat(result.isError()).isTrue();
        List<Booking> foundBookings = bookings.findAllBookingsByGuestName(new GuestName("Fritz"));
        assertThat(foundBookings).hasSize(0);
        assertThat(result.error().errorMessage()).isEqualTo("Guest cannot check in because they did not book a room");
    }

    @Test
    void checkIn_roomWasBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate1 = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate1,
                departureDate, new GuestName("Fritz"), roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);
        ArrivalDate arrivalDate2 = arrivalDate1.plusDays(17);

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate2);

        // THEN
        // FIXME !!!
        // assertThat(result.isError()).isFalse();
        // assertThat(result.result().size()).isEqualTo(0);
    }

    @Test
    void checkOut_roomWasBookedButNotCheckedIn_error() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz"), roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);

        // WHEN
        Either<Error, Booking> result =  service.checkOut(new GuestName("Fritz"), roomNumber1, departureDate);

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("Checkout only possible for invoiced bookings.");
    }

    @Test
    void checkOut_roomWasCheckedInButNotInvoiced_error() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz"), roomNumber1));
        HotelService service = new HotelService(setupRoomsWithNumbers(roomNumber1), bookings);
        service.checkIn(new GuestName("Fritz"), arrivalDate);

        // WHEN
        Either<Error, Booking> result = service.checkOut(new GuestName("Fritz"), roomNumber1, departureDate);

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("Checkout only possible for invoiced bookings.");
    }

    @Test
    void checkOut_roomWasInvoiced() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        BookingsRepository bookings = setupBookingsRepoWithBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz"), roomNumber1));
        RoomRepository rooms = setupRoomsWithNumbers(roomNumber1);
        HotelService service = new HotelService(rooms, bookings);
        service.checkIn(new GuestName("Fritz"), arrivalDate);

        PaymentRepository paymentRepository = new PaymentRepository();
        InvoiceRepository invoiceRepository = new InvoiceRepository();
        PaymentService paymentService = new PaymentService(paymentRepository, rooms, invoiceRepository, bookings);
        paymentService.payAmount(new GuestName("Fritz"), new Amount(200.0));
        paymentService.produceInvoice(new GuestName("Fritz"), departureDate, Collections.singletonList(roomNumber1));

        // WHEN
        Either<Error, Booking> result = service.checkOut(new GuestName("Fritz"), roomNumber1, departureDate);

        // THEN
        assertThat(result.isError()).isFalse();
        Assertions.assertThat(result.result().isCheckedOut()).isTrue();
    }
}
