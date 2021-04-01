package de.alewu.dsf.scanning.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alewu.dsf.exceptions.DungeonScannerException;
import de.alewu.dsf.file.RoomIdentificationFile;
import de.alewu.dsf.scanning.RoomType;
import de.alewu.dsf.scanning.identification.RoomIdentification;
import de.alewu.dsf.scanning.identification.RoomIdentificationMarker;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.minecraft.block.Block;

public class RoomIdentificationResolver implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(RoomIdentificationResolver.class.getName());
    private final RoomType type;
    private final List<RoomIdentification> identifiers;

    public RoomIdentificationResolver(RoomType type) {
        this.type = type;
        this.identifiers = new ArrayList<>();
        try {
            InputStreamReader reader = getFileReader();
            if (reader == null) {
                return;
            }
            JsonElement parseResult = new JsonParser().parse(reader);
            if (!parseResult.isJsonObject()) {
                throw new DungeonScannerException("Read json element of file '" + getFilePath(type) + "' is unable to be cast to a JsonObject");
            }
            readIdentifiers((JsonObject) parseResult);
        } catch (Exception e) {
            throw new DungeonScannerException("Could not read json file " + type.getMarkersFileName(), e);
        }
    }

    private void readIdentifiers(JsonObject data) {
        if (!data.has("entries")) {
            throw new DungeonScannerException("Invalid identifier file structure! 'entries' is missing");
        }
        for (JsonElement element : data.getAsJsonArray("entries")) {
            if (!element.isJsonObject()) {
                LOG.warning("Skipping one element (" + element.toString() + ") in file '" + getFilePath(type) + "' as it is not a json object");
                continue;
            }
            JsonObject obj = (JsonObject) element;
            if (!obj.has("id") || !obj.has("markers") || !obj.get("markers").isJsonArray() || obj.getAsJsonArray("markers").size() == 0) {
                LOG.warning("Skipping one element (" + element.toString() + ") in file '" + getFilePath(type) + "' as it is missing mandatory fields (id, markers)");
                continue;
            }
            String id = obj.get("id").getAsString();
            JsonArray markers = obj.getAsJsonArray("markers");
            List<RoomIdentificationMarker> markersList = new ArrayList<>();
            markers.forEach(x -> {
                if (!x.isJsonObject()) {
                    LOG.warning("Skipping one element (" + x.toString() + ") in markers array of marker '" + id
                        + "' in file '" + getFilePath(type) + "' as it is not a json object");
                    return;
                }
                JsonObject markerObj = (JsonObject) x;
                if (!markerObj.has("block") || !markerObj.has("relative")) {
                    LOG.warning("Skipping one element (" + x.toString() + ") in markers array of marker '" + id
                        + "' in file '" + getFilePath(type) + "' as it is missing mandatory fields (block, relative)");
                    return;
                }
                Block block = Block.getBlockFromName(markerObj.get("block").getAsString());
                RelativeLocation relativeLocation = new RelativeLocation(markerObj.get("relative").getAsString());
                markersList.add(new RoomIdentificationMarker(block, relativeLocation));
            });
            RoomIdentification identification = new RoomIdentification(id, markersList);
            identifiers.add(identification);
        }
    }

    public List<RoomIdentification> getIdentifiers() {
        return identifiers;
    }

    private InputStreamReader getFileReader() {
        if (type == null || type.getMarkersFileName() == null) {
            return null;
        }
        RoomIdentificationFile file = new RoomIdentificationFile(type);
        file.loadJsonObject();
        if (file.exists()) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream());
                LOG.info("Using external room identification file " + type.getMarkersFileName());
                return inputStreamReader;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return readInternalFile();
            }
        } else {
            return readInternalFile();
        }
    }

    private InputStreamReader readInternalFile() {
        InputStream resourceAsStream = getClass().getResourceAsStream(getFilePath(type));
        if (resourceAsStream == null) {
            throw new DungeonScannerException("No assets file found by name " + type.getMarkersFileName());
        }
        LOG.info("Using internal room identification file " + type.getMarkersFileName());
        return new InputStreamReader(resourceAsStream);
    }

    private String getFilePath(RoomType type) {
        return "/assets/dsf/identifiers/" + type.getMarkersFileName();
    }

    @Override
    public void close() {
        identifiers.clear();
    }
}
