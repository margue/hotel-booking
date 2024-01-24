package service;

import persistence.BookingInterval;
import persistence.Room;
import persistence.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class HotelService {

    private RoomRepository rooms;

    public HotelService(RoomRepository rooms) {
        this.rooms = rooms;
    }


    /*
    alle zimmer sind gleich
    jede nacht kostet 100.0

    USE CASES:
    - zimmerinformation erfragen (Verfügbarkeit, Preis)
    - zimmer buchen
    - zimmer zuweisen
    - einchecken
    - bezahlen
    - auschecken (inkl. rechnung)

    - stornieren
    - verlängern
    - verkürzen

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
     * @return price as double or null in case of no availability
     */
    public Double requestRoom(LocalDate startDate, LocalDate endDate){
        for(Room room : rooms.getRooms().values()){
            BookingInterval bookingInterval = new BookingInterval(startDate, endDate);
            if(room.roomIsFree(bookingInterval)){
                return 100.0 * bookingInterval.dates().size();
            }
        }
        return null;
    }
}
