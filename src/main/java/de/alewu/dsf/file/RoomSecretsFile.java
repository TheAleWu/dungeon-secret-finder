package de.alewu.dsf.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.DungeonScanner;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import de.alewu.dsf.util.RuntimeContext;

public class RoomSecretsFile extends AbstractFile {

    public RoomSecretsFile(DungeonRoom room) {
        super("/secrets", room.getIdentification().getRoomId() + ".json");
    }

    @Override
    protected void loadDefaults() {
        getObject().add("entries", new JsonArray());
    }

    public void addEntry(RoomSecret secret) {
        JsonArray array = getObject().getAsJsonArray("entries");
        if(array == null) {
            array = new JsonArray();
        }
        array.add(secret.toJsonObject());
        getObject().add("entries", array);
        save();
        rescan();
    }

    private void addEntry(int index, RoomSecret secret) {
        JsonArray array = getObject().getAsJsonArray("entries");
        JsonArray newArray = new JsonArray();
        if (array.size() == 0) {
            newArray.add(secret.toJsonObject());
        } else {
            if (index == array.size()) {
                for (int i = 0; i < array.size(); i++) {
                    newArray.add(array.get(i));
                }
                newArray.add(secret.toJsonObject());
            } else {
                for (int i = 0; i < array.size(); i++) {
                    if (i == index) {
                        newArray.add(secret.toJsonObject());
                    }
                    newArray.add(array.get(i));
                }
            }
        }
        getObject().add("entries", newArray);
        save();
    }

    public void removeEntry(int index) {
        if (getObject().has("entries")) {
            JsonArray array = getObject().getAsJsonArray("entries");
            JsonArray newArray = new JsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (i == index) {
                    continue;
                }
                JsonObject object = array.get(i).getAsJsonObject();
                newArray.add(object);
            }
            getObject().add("entries", newArray);
            save();
            rescan();
        }
    }

    public void updateEntry(RoomSecret secret) {
        int index = getIndex(secret);
        removeEntry(index);
        addEntry(index, secret);
        rescan();
    }

    private void rescan() {
        DungeonLayout currentDungeonLayout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        if (currentDungeonLayout != null) {
            DungeonScanner.scan(currentDungeonLayout.getIgnoredX(), currentDungeonLayout.getIgnoredZ());
        } else {
            DungeonScanner.scan(null, null);
        }
    }

    public int getIndex(RoomSecret secret) {
        JsonArray array = getObject().getAsJsonArray("entries");
        int index = -1;
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonObject()) {
                JsonObject obj = (JsonObject) element;
                if (obj.has("id") && obj.get("id").getAsString().equals(secret.getId().toString())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

}
