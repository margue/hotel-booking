package service;

import persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotelService {

    private RoomRepository rooms;

    public HotelService(RoomRepository rooms) {
        this.rooms = rooms;
    }


    /*
    TODO:
    - BookingInterval -> RoomBooking: GuestName, ArrivalDate, DepartureDate
    - BookingRequestInterval -> Trennung in ArrivalDate und DepartureDate
    - BookingsForRooms
    - BookingIntervals
    - RoomNumbers (Payment service)
    - PaymentRepository -> Payments
    - RoomRepository -> Rooms

    - contextive aufpumpen

    Verabredung:
    - Keine Trennung Buchung / tatsächlicher Aufenthalt

    ================================================================================
    - später Aggregat: BookingOfARoom

    ================================================================================

    ASSUMPTIONS:
    alle zimmer sind gleich
    jede nacht kostet 100.0
    ein Kunde ist eindeutig identifiziert durch einen einfachen String
    Sprachlevel Java 8 -> JUnit5 und JDK 17 zur Ausführung

    USE CASES:
    + zimmerinformation erfragen (Verfügbarkeit, Preis)
    + zimmer buchen
    [- zimmer zuweisen]
    + einchecken
    + zahlung leisten (auch mehrfach) (guestName) (Refactoring: Kunde hat Guthaben, Monolith, Closure of Operations)
    + rechnung erstellen (Legacy: Zahlungsausgleich bei der Erstellung, Refactoring: Rg. unbezahlt, Ausgleich im
    Nachgang)
    + zahlung leisten (nur wenn bisherige zahlungen < summe invoice)
    + auschecken (nur wenn invoice erstellt wurde)

---

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

---
    INTENTION REVEALING INTERFACES
    - Value Objects in "Interfaces" (Parameterlists) nutzen
        - Contextive (https://github.com/dev-cycles/contextive)
            -> Umweg über BookingRequestInterval zeigen
        - Ubiquitous Language
        - JMolecules -> @ValueObject
    - fachliche Operationen im Datenmodell einführen
        - Wiederverwendung von Value Objects
    - Optional
        - Clean Architecture
            - In- & Outports als Interfaces extrahieren
            - Interface je UseCase
            - JMolecules Onion/Hexagonal/etc.

     SIDE-EFFECT FREE FUNCTIONS
     - Either-Monade für Fehlerfälle
     - CQS (getInvoice)
     - Optional
        - Application vs Domain Service
        - Infrastructure(Repositories) in Services?

     ASSERTIONS
     - Design by Contract
     - Assertions (Pre(Guards) & Post Conditions):
        - Directly in programming language? Java assert <- Recherche
            - https://www.baeldung.com/java-assert
            - https://stackoverflow.com/questions/4624919/performance-drag-of-java-assertions-when-disabled
        - in Unit Tests
            - sind unsere Assertions in den Tests Assertions im Evans'schen Sinne (Zusicherungscharakter)?
     - Haben wir irgendwo Pre-/Post Conditions, die im Code verbuddelt sind und als solche schwer erkennbar sind
        - Dokumentiert diese mal, macht sie explizit zum Beispiel in Docs
     - Optional:
        - (in Docs)
        - Domänenmodell - ungültige Zustände nicht repräsentierbar machen (Scott Wlaschin)
            -> haben wir hier ein Beispiel dafür
     - Payment (fühlt sich komisch an)

     CONCEPTUAL CONTOURS
     - Rechnung ausstellen mit Zahlungsziel aktuell nicht möglich
     - Invoice kennt Raumsituation
     - Preisberechnung findet mehrfach statt

     STANDALONE CLASSES
     - fachliche Operationen -> Intention Revealing Interfaces
     - Logik fast ausschließlich in Services implementiert -> lässt sich davon noch was ins Domänenmodell verschieben
     -> standard computations

     CLOSURE OF OPERATIONS
     - Preisberechnung als Monoid (Rückverweis auf standalone classes)
        -> Es besteht auch die Möglichkeit, Monoide als abstraktes Konzept zu implementieren

     Optionaler Test:
     - Wie aufwendig ist es, in unsere refactorte Code Base Rabatte einzubauen?
     */

    /**
     * Welcome to Hilberts Hotel!
     *
     * @return price as double or null in case of no availability
     */
    public Double requestRoom(BookingRequestInterval bookingRequestInterval) {
        for (Room room : rooms.getRooms().values()) {
            BookingInterval bookingInterval = new BookingInterval(bookingRequestInterval.startDate(), bookingRequestInterval.endDate());
            if (room.roomIsFree(bookingInterval)) {
                return 100.0 * bookingInterval.dates().size();
            }
        }
        return null;
    }

    public void bookRoom(BookingRequestInterval bookingRequestInterval, GuestName guestName) {
        if (guestName.guestName() == null) {
            throw new IllegalArgumentException("Guest name must not be null");
        }
        for (Room room : rooms.getRooms().values()) {
            BookingInterval bookingInterval = new BookingInterval(bookingRequestInterval.startDate(), bookingRequestInterval.endDate(), guestName);
            if (room.roomIsFree(bookingInterval)) {
                room.getBookings().add(bookingInterval); // no validation (race condition?)
                rooms.save(room); // not needed here, but generally required for persistence
                return;
            }
        }
        throw new IllegalStateException("No rooms available on the given date(s)");
    }

    public List<RoomNumber> checkIn(GuestName guestName, ArrivalDate arrivalDate) {
        List<Room> roomsForGuest = rooms.findAllRoomsWithBookingIntervalsByGuestName(guestName);
        if (roomsForGuest.size() == 0) {
            throw new IllegalStateException("Guest cannot check in because they did not book a room");
        }
        List<RoomNumber> bookedRoomNumbers = new ArrayList<>();
        roomsForGuest.forEach(room -> {
            List<BookingInterval> currentBookings = room.getBookings().stream()
                    .filter(interval -> interval.getGuestName().equals(guestName))
                    .filter(interval -> interval.getStartDate().equals(arrivalDate.date()))
                    .toList();
            if (currentBookings.size() > 0) {
                currentBookings.forEach(interval -> interval.setCheckedIn(true));
                bookedRoomNumbers.add(room.getRoomNumber());
                rooms.save(room);
            }
        });
        return bookedRoomNumbers;
    }

    public void checkOut(GuestName guestName, RoomNumber roomNumber, DepartureDate departureDate) {
        Room room = rooms.getRooms().get(roomNumber);
        List<BookingInterval> bookingsToCheckOut = room.getBookings().stream()
                .filter(interval -> Objects.equals(interval.getGuestName(), guestName))
                .filter(interval -> interval.getEndDate().equals(departureDate.date())).toList();
        if(bookingsToCheckOut.size() == 0){
            throw new IllegalStateException("No booking to be checked out!");
        }
        if(bookingsToCheckOut.size() > 1){
            throw new IllegalStateException("More than one booking found!");
        }
        BookingInterval booking = bookingsToCheckOut.getFirst();
        if(!booking.isInvoiced()){
            throw new IllegalStateException("Checkout only possible for invoiced bookings.");
        }
        booking.setCheckedOut(true);
        rooms.save(room);
    }
}
