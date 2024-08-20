package service;

import persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotelService {

    private final RoomRepository rooms;

    public HotelService(RoomRepository rooms) {
        this.rooms = rooms;
    }


    /*
    TODO:
    - BookingsForRooms in Invoice -> das räumt dann gleich alles auf ^^

    - RoomRepository macht auch Fachliches...

    - contextive aufpumpen

    Verabredung:
    - Keine Trennung Buchung / tatsächlicher Aufenthalt

    ================================================================================
    später:
    - Aggregat: BookingOfARoom
    - Invoice als Record

    später??:
    - PaymentRepository -> Payments
    - RoomRepository -> Rooms

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
    + Value Objects in "Interfaces" (Parameterlists) nutzen
        - Contextive (https://github.com/dev-cycles/contextive)
            -> Umweg über BookingRequestInterval zeigen
    - Ubiquitous Language
    + fachliche Operationen im Datenmodell einführen
        - Wiederverwendung von Value Objects
    - Optional
        - JMolecules -> @ValueObject
        - Clean Architecture
            - In- & Outports als Interfaces extrahieren
            - Interface je UseCase
            - JMolecules Onion/Hexagonal/etc.

     SIDE-EFFECT FREE FUNCTIONS
     + Either-Monade für Fehlerfälle
        - https://gist.github.com/colinwd/503cf0d49ed5e26cc92bd791c12bbfb4 (Bug in l. 43?)
        - https://www.baeldung.com/java-monads
        - HotelService requestRoom vs. bookRoom
        -> zunächst mal nur mit Tupel
     + CQS (getInvoice)
        - implement repository for Invoices, persist there
    - Payment side-effect-free machen
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
     -> nur theoretisch abhandeln, kritische Fragen stellen
     - Passt unser Modell zur Realität?
     - Rechnung ausstellen mit Zahlungsziel aktuell nicht möglich
     - Invoice kennt Raumsituation
     - Bookings kennen Raumsituation
     - Preisberechnung findet mehrfach statt
     - Implizites Kontensystem über die Zahlungen
     - Gast ist noch keine richtige Entität

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
     * @return price as Amount or Error in case of no availability
     *
     * Precondition: There must be a room available for the given dates
     */
    public Either<Error, Amount> requestRoom(ArrivalDate arrivalDate, DepartureDate departureDate) {
        for (Room room : rooms.getRooms().values()) {
            if (room.roomIsFree(arrivalDate, departureDate)) {
                return Either.ofResult(new Amount(100.0 * arrivalDate.daysUntil(departureDate.departureDate())));
            }
        }
        return Either.ofError(new Error("No available room found for the desired dates"));
    }

    /*
    Precondition: Guest name must not be null
    Precondition: There must be a room available for the given dates

    Postcondition: Guest has booked a room.
     */
    public Either<Error, RoomNumber> bookRoom(ArrivalDate arrivalDate, DepartureDate departureDate, GuestName guestName) {
        if (guestName.guestName() == null) {
            return Either.ofError(new Error("Guest name must not be null"));
        }
        Booking booking = new Booking(arrivalDate, departureDate, guestName);
        for (Room room : rooms.getRooms().values()) {
            if (room.roomIsFree(arrivalDate, departureDate)) {
                room.getBookings().add(booking); // no validation (race condition?)
                rooms.save(room); // not needed here, but generally required for persistence
                return Either.ofResult(room.getRoomNumber());
            }
        }
        return Either.ofError(new Error("No rooms available on the given date(s)"));
    }

    /*
    Precondition: Guest must have booked one or more rooms for the given arrival date

    Postcondition: Guest is checked in.
     */
    public Either<Error, List<RoomNumber>> checkIn(GuestName guestName, ArrivalDate arrivalDate) {
        List<Room> roomsForGuest = rooms.findAllRoomsWithBookingsByGuestName(guestName);
        if (roomsForGuest.size() == 0) {
            return Either.ofError(new Error("Guest cannot check in because they did not book a room"));
        }
        List<RoomNumber> bookedRoomNumbers = new ArrayList<>();
        roomsForGuest.forEach(room -> {
            List<Booking> currentBookings = room.getBookings().stream()
                    .filter(booking -> booking.getGuestName().equals(guestName))
                    .filter(booking -> booking.getArrivalDate().equals(arrivalDate))
                    .toList();
            if (currentBookings.size() > 0) {
                currentBookings.forEach(booking -> booking.setCheckedIn(true));
                bookedRoomNumbers.add(room.getRoomNumber());
                rooms.save(room);
            }
        });
        return Either.ofResult(bookedRoomNumbers);
    }

    /*
    Precondition: There must be a booking that the guest can check out of
    Precondition: There must be exactly one such booking
    Precondition: The booking must have been invoiced before checkout

    Postcondition: Guest is checked out of room
     */
    public Either<Error, Booking> checkOut(GuestName guestName, RoomNumber roomNumber, DepartureDate departureDate) {
        Room room = rooms.getRooms().get(roomNumber);
        List<Booking> bookingsToCheckOut = room.getBookings().stream()
                .filter(booking -> Objects.equals(booking.getGuestName(), guestName))
                .filter(booking -> booking.getDepartureDate().equals(departureDate)).toList();
        if(bookingsToCheckOut.size() == 0){
            return Either.ofError(new Error("No booking to be checked out!"));
        }
        if(bookingsToCheckOut.size() > 1){
            return Either.ofError(new Error("More than one booking found!"));
        }
        Booking booking = bookingsToCheckOut.getFirst();
        if(!booking.isInvoiced()){
            return Either.ofError(new Error("Checkout only possible for invoiced bookings."));
        }
        booking.setCheckedOut(true);
        rooms.save(room);
        return Either.ofResult(booking);
    }
}
