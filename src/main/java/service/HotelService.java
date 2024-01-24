package service;

import persistence.BookingInterval;
import persistence.Room;
import persistence.RoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class HotelService {

    private RoomRepository rooms;

    public HotelService(RoomRepository rooms) {
        this.rooms = rooms;
    }


    /*
    ASSUMPTIONS:
    alle zimmer sind gleich
    jede nacht kostet 100.0
    ein Kunde ist eindeutig identifiziert durch einen einfachen String

    USE CASES:
    + zimmerinformation erfragen (Verfügbarkeit, Preis)
    + zimmer buchen
    [- zimmer zuweisen]
    + einchecken
    - zahlung leisten (auch mehrfach)
    - rechnung erstellen
    - zahlung leisten (nur wenn bisherige zahlungen < summe invoice)
    - auschecken (nur wenn zahlungen >= summe invoice)

    - stornieren (nur wenn nicht eingecheckt. rückbuchung?)
    - verlängern (nur wenn nicht ausgecheckt und rechnung noch nicht erstellt)
    - verkürzen (nur wenn nicht ausgecheckt und rechnung noch nicht erstellt, nur wenn Datum noch nicht erreicht)

---

    - buchen "meines" Zimmers (Stammgast)
    - als Gruppe buchen

    - preis wird an die auslastung angepasst

    - reinigung
        - zur reinigung freigeben
        - reinigung bestätigen/zum neubezug freigeben
    - wartung
        - zimmer jetzt blockieren
        - zimmer geplant blockieren
        - zimmer wieder freigeben
     */

    /**
     * Welcome to Hilberts Hotel!
     *
     * @return price as double or null in case of no availability
     */
    public Double requestRoom(LocalDate startDate, LocalDate endDate) {
        for (Room room : rooms.getRooms().values()) {
            BookingInterval bookingInterval = new BookingInterval(startDate, endDate);
            if (room.roomIsFree(bookingInterval)) {
                return 100.0 * bookingInterval.dates().size();
            }
        }
        return null;
    }

    public void bookRoom(LocalDate startDate, LocalDate endDate, String customerName) {
        if (customerName == null) {
            throw new IllegalArgumentException("Customer name must not be null");
        }
        for (Room room : rooms.getRooms().values()) {
            BookingInterval bookingInterval = new BookingInterval(startDate, endDate, customerName);
            if (room.roomIsFree(bookingInterval)) {
                room.getBookings().add(bookingInterval); // no validation (race condition?)
                rooms.save(room); // not needed here, but generally required for persistence
                return;
            }
        }
        throw new IllegalStateException("No rooms available on the given date(s)");
    }

    public void checkIn(String customerName, LocalDate startDate) {
        List<Room> roomsForCustomer = rooms.findAllRoomsWithBookingIntervalsByCustomerName(customerName);
        if (roomsForCustomer.size() == 0) {
            throw new IllegalStateException("Customer cannot check in because they did not book a room");
        }
        roomsForCustomer.forEach(room -> {
            room.getBookings().stream()
                    .filter(interval -> interval.getCustomerName().equals(customerName))
                    .filter(interval -> interval.getStartDate().equals(startDate))
                    .forEach(interval -> interval.setIsCheckedIn(true));
            rooms.save(room);
        });
    }
}
