package persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {

    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public void save(Room room){
        rooms.put(room.getRoomNumber(), room);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }
}
