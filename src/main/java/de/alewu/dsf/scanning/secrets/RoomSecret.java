package de.alewu.dsf.scanning.secrets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.secrets.conditions.SecretCondition;
import de.alewu.dsf.scanning.shared.JsonConvertible;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.AxisAlignedBB;
import org.apache.commons.lang3.EnumUtils;

public class RoomSecret extends JsonConvertible {

    private final UUID id;
    private SecretType secretType;
    private RelativeLocation relativeLocation;
    private String comment;
    private final List<SecretCondition> conditions;
    private AxisAlignedBB borderSize;
    private Color markerColor;
    private UUID referencedId;
    private RoomSecret referenced;

    public RoomSecret(UUID id, SecretType secretType, RelativeLocation relativeLocation, String comment, List<SecretCondition> conditions,
        AxisAlignedBB borderSize, Color markerColor, UUID referencedId) {
        this.id = id;
        this.secretType = secretType;
        this.relativeLocation = relativeLocation;
        this.comment = comment;
        this.conditions = conditions;
        this.borderSize = (borderSize != null ? borderSize : secretType.getDefaultBorderSize());
        this.markerColor = (markerColor != null ? markerColor : secretType.getDefaultMarkerColor().getColor());
        this.referencedId = referencedId;
    }

    public UUID getId() {
        return id;
    }

    public SecretType getSecretType() {
        return secretType;
    }

    public RelativeLocation getRelativeLocation() {
        return relativeLocation;
    }

    public String getComment() {
        return comment;
    }


    public AxisAlignedBB getBorderSize() {
        return borderSize;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public RoomSecret getReferenced() {
        return referenced;
    }

    public void setReferenced(RoomSecret referencedBy) {
        if (referencedBy != null) {
            this.referenced = referencedBy;
            this.referencedId = referenced.getId();
        } else {
            this.referenced = null;
            this.referencedId = null;
        }
    }

    public UUID getReferencedId() {
        return referencedId;
    }

    public void setSecretType(SecretType secretType) {
        this.secretType = secretType;
    }

    public void setRelativeLocation(RelativeLocation relativeLocation) {
        this.relativeLocation = relativeLocation;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<SecretCondition> getConditions() {
        return conditions;
    }

    public void setBorderSize(AxisAlignedBB borderSize) {
        this.borderSize = borderSize;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public List<RoomSecret> getSubordinateSecretChain() {
        List<RoomSecret> secrets = new ArrayList<>();
        RoomSecret nextSecret = getReferenced();
        while (nextSecret != null) {
            secrets.add(nextSecret);
            nextSecret = nextSecret.getReferenced();
        }
        return secrets;
    }

    public boolean isRootSecret(List<RoomSecret> allSecrets) {
        return getRootSecret(this, allSecrets) == null;
    }

    public RoomSecret getRootSecret(List<RoomSecret> allSecrets) {
        RoomSecret current = getRootSecret(this, allSecrets);
        while (true) {
            RoomSecret superOrdinate = getRootSecret(current, allSecrets);
            if (superOrdinate != null) {
                current = superOrdinate;
                continue;
            }
            return current;
        }
    }

    // Prevent stack overflow
    private RoomSecret getRootSecret(RoomSecret roomSecret, List<RoomSecret> allSecrets) {
        return allSecrets.stream().filter(x -> x.getReferencedId() != null && x.getReferencedId().equals(roomSecret.getId())).findFirst().orElse(null);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id.toString());
        object.addProperty("type", secretType.toString());
        RelativeLocation l = this.relativeLocation;
        object.addProperty("relative", l.getX() + ";" + l.getY() + ";" + l.getZ());
        object.addProperty("comment", comment);
        AxisAlignedBB aabb = this.borderSize;
        object.addProperty("aabb", aabb.minX + ";" + aabb.minY + ";" + aabb.minZ + ";" + aabb.maxX + ";" + aabb.maxY + ";" + aabb.maxZ);
        object.addProperty("rgb", markerColor.getRed() + ";" + markerColor.getGreen() + ";" + markerColor.getBlue());
        JsonArray conditionArray = new JsonArray();
        for (SecretCondition condition : conditions) {
            JsonObject conditionObject = new JsonObject();
            condition.getConditionData().entrySet().forEach(e -> conditionObject.add(e.getKey(), e.getValue()));
            conditionArray.add(conditionObject);
        }
        object.add("conditions", conditionArray);
        if (referencedId != null) {
            object.addProperty("referencedId", referencedId.toString());
        }

        return object;
    }

    private static AxisAlignedBB aabb(String str) {
        if (str == null) {
            return null;
        }
        String[] args = str.split(";");
        if (args.length != 6) {
            return null;
        }
        int x1 = Integer.parseInt(args[0]);
        int y1 = Integer.parseInt(args[1]);
        int z1 = Integer.parseInt(args[2]);
        int x2 = Integer.parseInt(args[3]);
        int y2 = Integer.parseInt(args[4]);
        int z2 = Integer.parseInt(args[5]);
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    private static Color color(String str) {
        if (str == null) {
            return null;
        }
        String[] args = str.split(";");
        if (args.length == 3) {
            return null;
        }
        return new Color(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    private static List<SecretCondition> conditions(DungeonRoom room, JsonArray array) {
        List<SecretCondition> conditions = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = (JsonObject) element;
            SecretConditionType type = EnumUtils.getEnum(SecretConditionType.class, obj.get("type").getAsString());
            conditions.add(new SecretCondition(room, type, obj));
        }
        return conditions;
    }
}
