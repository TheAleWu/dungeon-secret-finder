package de.alewu.dsf.scanning.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alewu.dsf.exceptions.DungeonScannerException;
import de.alewu.dsf.file.RoomSecretsFile;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import de.alewu.dsf.scanning.secrets.SecretConditionType;
import de.alewu.dsf.scanning.secrets.SecretType;
import de.alewu.dsf.scanning.secrets.conditions.SecretCondition;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import net.minecraft.util.AxisAlignedBB;
import org.apache.commons.lang3.EnumUtils;

public class RoomSecretsResolver implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(RoomSecretsResolver.class.getName());
    private final DungeonRoom room;
    private final List<RoomSecret> secrets;

    public RoomSecretsResolver(DungeonRoom room) {
        this.room = room;
        String roomIdentification = room.getIdentification().getRoomId();
        try {
            InputStreamReader reader = getFileReader();
            JsonElement parseResult = new JsonParser().parse(reader);
            if (!parseResult.isJsonObject()) {
                throw new DungeonScannerException("Read json element of file '" + getFilePath(roomIdentification) + "' is unable to be cast to a JsonObject");
            }
            this.secrets = new ArrayList<>();
            readSecrets((JsonObject) parseResult);
        } catch (Exception e) {
            throw new DungeonScannerException("Could not read json file " + roomIdentification + ".json", e);
        }
    }

    private void readSecrets(JsonObject data) {
        if (!data.has("entries")) {
            throw new DungeonScannerException("Invalid identifier file structure! 'entries' is missing");
        }
        String roomIdentification = room.getIdentification().getRoomId();
        String filePath = getFilePath(roomIdentification);
        List<RoomSecret> parsedSecrets = new ArrayList<>();
        for (JsonElement element : data.getAsJsonArray("entries")) {
            if (!element.isJsonObject()) {
                LOG.warning("Skipping one element (" + element.toString() + ") in file '" + filePath + "' as it is not a json object");
                continue;
            }
            JsonObject obj = (JsonObject) element;
            if (!obj.has("id") || !obj.has("type") || !obj.has("relative")) {
                LOG.warning("Skipping one element (" + obj.toString() + ") in file '" + filePath + "' as it is missing mandatory fields (id, type, relative)");
                continue;
            }
            UUID id = UUID.fromString(obj.get("id").getAsString());
            SecretType secretType = EnumUtils.getEnum(SecretType.class, obj.get("type").getAsString());
            RelativeLocation loc = new RelativeLocation(obj.get("relative").getAsString());
            String comment = (obj.has("comment") ? obj.get("comment").getAsString() : null);
            String[] aabb = (obj.has("aabb") ? obj.get("aabb").getAsString().split(";") : new String[0]);
            AxisAlignedBB border = null;
            if (aabb.length == 6) {
                border = new AxisAlignedBB(Float.parseFloat(aabb[0]), Float.parseFloat(aabb[1]), Float.parseFloat(aabb[2]),
                    Float.parseFloat(aabb[3]), Float.parseFloat(aabb[4]), Float.parseFloat(aabb[5]));
            }
            String[] color = (obj.has("rgb") ? obj.get("rgb").getAsString().split(";") : new String[0]);
            Color borderColor = null;
            if (color.length == 3) {
                borderColor = new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
            }
            UUID referencedId = (obj.has("referencedId") ? UUID.fromString(obj.get("referencedId").getAsString()) : null);
            List<SecretCondition> conditions = new ArrayList<>();
            if (obj.has("conditions")) {
                JsonElement conditionsElement = obj.get("conditions");
                if (!conditionsElement.isJsonArray()) {
                    LOG.warning("Malformed element (" + obj.toString() + "). Contains conditions tree but conditions is not a json array ("
                        + conditionsElement.toString() + ")");
                    continue;
                }
                for (JsonElement conditionElement : conditionsElement.getAsJsonArray()) {
                    if (!conditionElement.isJsonObject()) {
                        LOG.warning("Malformed element (" + conditionElement + "). Expected element to be a json object. Skipping this condition");
                        continue;
                    }
                    JsonObject condition = (JsonObject) conditionElement;
                    if (condition.has("t")) {
                        String conditionType = condition.get("t").getAsString();
                        SecretConditionType type = EnumUtils.getEnum(SecretConditionType.class, conditionType);
                        if (type == null) {
                            LOG.warning("Malformed element (" + conditionElement + "). Condition type " + conditionType + " not mapped. Skipping this condition");
                            continue;
                        }
                        conditions.add(new SecretCondition(room, type, condition));
                    }
                }
            }
            parsedSecrets.add(new RoomSecret(id, secretType, loc, comment, conditions, border, borderColor, referencedId));
        }
        for (RoomSecret secret : parsedSecrets) {
            parsedSecrets.stream().filter(x -> secret.getReferencedId() != null && secret.getReferencedId().equals(x.getId()))
                .findFirst()
                .ifPresent(secret::setReferenced);
            secrets.add(secret);
        }
    }

    public List<RoomSecret> getSecrets() {
        return secrets;
    }

    private InputStreamReader getFileReader() {
        RoomSecretsFile file = new RoomSecretsFile(room);
        file.loadJsonObject();
        if (file.exists()) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream());
                LOG.info("Using external room identification file " + room.getIdentification().getRoomId() + ".json");
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
        InputStream resourceAsStream = getClass().getResourceAsStream(getFilePath(room.getIdentification().getRoomId()));
        if (resourceAsStream == null) {
            throw new DungeonScannerException("No assets file found by name " + room.getIdentification().getRoomId() + ".json");
        }
        LOG.info("Using internal room identification file " + room.getIdentification().getRoomId() + ".json");
        return new InputStreamReader(resourceAsStream);
    }

    private String getFilePath(String roomIdentification) {
        return "/assets/dsf/secrets/" + roomIdentification + ".json";
    }

    @Override
    public void close() {
        secrets.clear();
    }
}
