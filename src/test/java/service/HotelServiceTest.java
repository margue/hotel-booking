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
        BookingsRepository bookings = new BookingsRepository();
        for (int i = 1; i <= numberOfRooms; i++) {
            bookings.save(new BookingsForRoom(new RoomNumber(Integer.toString(i))));
        }
        return new HotelService(new RoomRepository(), bookings);
    }

    public RoomRepository setupRoomsWithOneRoomAndBookings(Booking... bookings){
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room(new RoomNumber("1"), new ArrayList<>(Arrays.asList(bookings))));
        return rooms;
    }

    public BookingsRepository setupBookingsForRoomsWithOneRoomAndBookings(Booking... bookings){
        BookingsRepository bookingsRepository = new BookingsRepository();
        BookingsForRoom bookingsForRoom = new BookingsForRoom(new RoomNumber("1"));
        bookingsForRoom.add(Arrays.stream(bookings).toList());
        bookingsRepository.save(bookingsForRoom);
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
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                        departureDate, guestWithBooking)), HotelService.buildBookingsRepository(setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                        departureDate, guestWithBooking))));

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
        HotelService service = new HotelService(setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate.plusDays(5), departureDate.plusDays(7), guestWithBooking)), HotelService.buildBookingsRepository(setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate.plusDays(5), departureDate.plusDays(7), guestWithBooking))));

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
        BookingsRepository bookings = setupBookingsForRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Peter"));

        // THEN
        assertThat(result.isError()).isFalse();
        List<BookingsForRoom> foundBookings = bookings.findBookingsForRoomsWithBookingFor(new GuestName("Peter"));
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst().bookings().getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.getFirst().bookings().getFirst().getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_bookTwoRoomsForSameNights() {
        // GIVEN
        RoomRepository rooms = new RoomRepository();
        rooms.save(new Room(roomNumber1));
        rooms.save(new Room(roomNumber2));
        BookingsRepository bookings = new BookingsRepository();
        bookings.save(new BookingsForRoom(roomNumber1));
        bookings.save(new BookingsForRoom(roomNumber2));
        HotelService service = new HotelService(rooms, bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);

        GuestName peter = new GuestName("Peter");
        Either<Error, RoomNumber> result1 = service.bookRoom(arrivalDate, departureDate, peter);
        assertThat(result1.isError()).isFalse();

        // WHEN
        Either<Error, RoomNumber> result2 = service.bookRoom(arrivalDate, departureDate, peter);

        // THEN
        assertThat(result2.isError()).isFalse();
        List<BookingsForRoom> foundRooms = bookings.findBookingsForRoomsWithBookingFor(peter);
        assertThat(foundRooms).hasSize(2);
        assertThat(foundRooms).extracting("roomNumber")
                        .containsExactlyInAnyOrder(roomNumber1, roomNumber2);
        assertThat(foundRooms.get(0).bookings().get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundRooms.get(0).bookings().get(0).getDepartureDate()).isEqualTo(departureDate);
        assertThat(foundRooms.get(1).bookings().get(0).getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundRooms.get(1).bookings().get(0).getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_roomAvailableForMultipleNights() {
        // GIVEN
        BookingsRepository bookings = setupBookingsForRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(bookings);
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Fred"));

        // THEN
        assertThat(result.isError()).isFalse();
        List<BookingsForRoom> foundBookings = bookings.findBookingsForRoomsWithBookingFor(new GuestName("Fred"));
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst().bookings().getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.getFirst().bookings().getFirst().getDepartureDate()).isEqualTo(departureDate);
    }

    @Test
    void bookRoom_roomNotAvailable() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        Booking booking1 = new Booking(arrivalDate,
                departureDate, guestWithBooking);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(booking1);
        BookingsRepository bookings = setupBookingsForRoomsWithOneRoomAndBookings(booking1);
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));

        // WHEN
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, new GuestName("Jack"));

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("No rooms available on the given date(s)");
        // no accidental changes to rooms:
        List<BookingsForRoom> foundBookings = bookings.findBookingsForRoomsWithBookingFor(new GuestName("Jack"));
        assertThat(foundBookings).hasSize(0);
    }

    @Test
    void bookRoom_roomAvailableAlthoughBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 11);
        Booking booking1 = new Booking(arrivalDate.plusDays(5), departureDate.plusDays(7), guestWithBooking);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(booking1);
        BookingsRepository bookings = setupBookingsForRoomsWithOneRoomAndBookings(booking1);
        HotelService service = new HotelService(rooms, bookings);

        // WHEN
        GuestName jim = new GuestName("Jim");
        Either<Error, RoomNumber> result = service.bookRoom(arrivalDate, departureDate, jim);

        // THEN
        assertThat(result.isError()).isFalse();
        List<BookingsForRoom> foundBookingsForRooms = bookings.findBookingsForRoomsWithBookingFor(jim);
        assertThat(foundBookingsForRooms).hasSize(1);
        List<Booking> foundBookings = foundBookingsForRooms.getFirst().getBookingsFor(jim);
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst().getArrivalDate()).isEqualTo(arrivalDate);
        assertThat(foundBookings.getFirst().getDepartureDate()).isEqualTo(departureDate);
        // do some checks with the room?
    }

    @Test
    void checkIn_roomWasBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate);

        // THEN
        assertThat(result.isError()).isFalse();
        assertThat(result.result().size()).isEqualTo(1);
        assertThat(result.result().getFirst().number()).isEqualTo("1");
    }

    @Test
    void checkIn_roomWasNotBooked() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        BookingsRepository bookings = setupBookingsForRoomsWithOneRoomAndBookings();
        HotelService service = new HotelService(bookings);

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate);

        // THEN
        assertThat(result.isError()).isTrue();
        List<BookingsForRoom> foundBookings = bookings.findBookingsForRoomsWithBookingFor(new GuestName("Fritz"));
        assertThat(foundBookings).hasSize(0);
        assertThat(result.error().errorMessage()).isEqualTo("Guest cannot check in because they did not book a room on ArrivalDate[arrivalDate=2020-10-10]");
    }

    @Test
    void checkIn_roomWasBookedOnDifferentDate() {
        // GIVEN
        ArrivalDate arrivalDate1 = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate1,
                departureDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));
        ArrivalDate arrivalDate2 = arrivalDate1.plusDays(17);

        // WHEN
        Either<Error, List<RoomNumber>> result = service.checkIn(new GuestName("Fritz"), arrivalDate2);

        // THEN
        assertThat(result.isError()).isTrue();
        assertThat(result.error().errorMessage()).isEqualTo("Guest cannot check in because they did not book a room on ArrivalDate[arrivalDate=2020-10-27]");
    }

    @Test
    void checkOut_roomWasBookedButNotCheckedIn_error() {
        // GIVEN
        ArrivalDate arrivalDate = new ArrivalDate(2020, 10, 10);
        DepartureDate departureDate = new DepartureDate(2020, 10, 12);
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));

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
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));
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
        RoomRepository rooms = setupRoomsWithOneRoomAndBookings(new Booking(arrivalDate,
                departureDate, new GuestName("Fritz")));
        HotelService service = new HotelService(rooms, HotelService.buildBookingsRepository(rooms));
        service.checkIn(new GuestName("Fritz"), arrivalDate);

        PaymentRepository paymentRepository = new PaymentRepository();
        InvoiceRepository invoiceRepository = new InvoiceRepository();
        PaymentService paymentService = new PaymentService(paymentRepository, HotelService.buildBookingsRepository(rooms), invoiceRepository);
        paymentService.payAmount(new GuestName("Fritz"), new Amount(200.0));
        paymentService.produceInvoice(new GuestName("Fritz"), departureDate, Collections.singletonList(roomNumber1));

        // WHEN
        Either<Error, Booking> result = service.checkOut(new GuestName("Fritz"), roomNumber1, departureDate);

        // THEN
        assertThat(result.isError()).isFalse();
        Assertions.assertThat(result.result().isCheckedOut()).isTrue();
    }

}
