package de.alewu.dsf.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.RoomType;
import de.alewu.dsf.scanning.identification.RoomIdentification;
import java.util.stream.StreamSupport;

public class RoomIdentificationFile extends AbstractFile {

    public RoomIdentificationFile(RoomType roomType) {
        super("/identification", roomType.getMarkersFileName());
    }

    @Override
    protected void loadDefaults() {
        getObject().add("entries", new JsonArray());
    }

    public void addEntry(RoomIdentification identification) {
        if (getObject().has("entries")) {
            JsonArray array = getObject().getAsJsonArray("entries");
            boolean notExists = StreamSupport.stream(array.spliterator(), false).noneMatch(x -> {
                JsonObject obj = x.getAsJsonObject();
                return obj.has("id") && obj.get("id").getAsString().equals(identification.getRoomId());
            });
            if (notExists) {
                array.add(identification.toJsonObject());
                getObject().add("entries", array);
                save();
            }
        }
    }

    public void removeEntry(String id) {
        if (getObject().has("entries")) {
            JsonArray array = getObject().getAsJsonArray("entries");
            JsonArray newArray = new JsonArray();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("id") && !object.get("id").getAsString().equals(id)) {
                    newArray.add(object);
                }
            }
            getObject().add("entries", newArray);
            save();
        }
    }

}
