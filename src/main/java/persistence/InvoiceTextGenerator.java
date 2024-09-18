package persistence;

import java.util.List;
import java.util.Map;

// Domain Service
public class InvoiceTextGenerator {
    public static List<String> textForInvoice(Map<RoomNumber, List<Booking>> bookingsForRooms){
        return bookingsForRooms.keySet().stream().map(
                roomNumber -> bookingsForRooms.get(roomNumber).stream().map(booking -> String.format("Room number %s from %s to %s", roomNumber.number(), booking.getArrivalDate(), booking.getDepartureDate())).toList()
        ).flatMap(List::stream).toList();
    }
}
