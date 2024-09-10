package persistence;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {

    private final Map<RoomNumber, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room) {
        rooms.put(room.roomNumber(), room);
    }

    public Map<RoomNumber, Room> getRooms() {
        return rooms;
    }
}
