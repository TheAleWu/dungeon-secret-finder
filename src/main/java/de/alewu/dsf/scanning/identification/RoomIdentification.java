package de.alewu.dsf.scanning.identification;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.shared.JsonConvertible;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import java.util.List;

public class RoomIdentification extends JsonConvertible {

    private final String roomId;
    private final List<RoomIdentificationMarker> markers;

    public RoomIdentification(String roomId, List<RoomIdentificationMarker> markers) {
        this.roomId = roomId;
        this.markers = markers;
    }

    public String getRoomId() {
        return roomId;
    }

    public List<RoomIdentificationMarker> getMarkers() {
        return markers;
    }


    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", roomId);
        JsonArray markersArray = new JsonArray();
        for (RoomIdentificationMarker marker : markers) {
            JsonObject markersObject = new JsonObject();
            markersObject.addProperty("block", marker.getBlock().getRegistryName());
            RelativeLocation l = marker.getLocation();
            markersObject.addProperty("relative", l.getX() + ";" + l.getY() + ";" + l.getZ());
            markersArray.add(markersObject);
        }
        object.add("markers", markersArray);
        return object;
    }
}
