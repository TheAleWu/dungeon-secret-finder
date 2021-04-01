package de.alewu.dsf.commands;

import static de.alewu.dsf.util.Communication.chatMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alewu.dsf.exceptions.DungeonSecretFinderException;
import de.alewu.dsf.file.RoomIdentificationFile;
import de.alewu.dsf.file.RoomSecretsFile;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.RoomType;
import de.alewu.dsf.scanning.identification.RoomIdentification;
import de.alewu.dsf.scanning.identification.RoomIdentificationMarker;
import de.alewu.dsf.scanning.resolvers.RoomIdentificationResolver;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import de.alewu.dsf.scanning.secrets.SecretType;
import de.alewu.dsf.scanning.secrets.conditions.SecretCondition;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import de.alewu.dsf.util.ColorMap;
import de.alewu.dsf.util.ColorMap.ColorData;
import de.alewu.dsf.util.RoomDisplayContext;
import de.alewu.dsf.util.RoomDisplayContext.DisplayOption;
import de.alewu.dsf.util.RuntimeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.EnumUtils;

public class RoomDesignerLogic {

    public static void showHelp() {
        chatMessage("");
        chatMessage("§5§lDSF Room Designer");
        chatMessage("§7Used as §e/dsf rd <subcommand>");
        chatMessage("");
        chatMessage("§9§lSubcommands:");
        chatMessage("§6toggle-display §7Toggle between secrets and markers");
        chatMessage("§6identify-by <Name> §7Create or rename room identification");
        chatMessage("§6define-marker §7Add or remove a marker to the current room");
        chatMessage("§6list-secrets §7Shows all secrets");
        chatMessage("§6new-secret §7Create a new secret");
        chatMessage("§6delete-secret <Id> §7Delete a given secret by its id");
        chatMessage("§6add-step <Id> §7Add a new step at the targeted block");
        chatMessage("§6delete-step <Id> <Step> §7Delete the given step");
        chatMessage("§6set-type <Id> <Step|\"root\"> <Type> §7Change the type of a step");
        chatMessage("§6move <Id> <Step|\"root\"> [x;y;z] §7Move step [to given relative]");
        chatMessage("§6comment <Id> <Step|\"root\"> <Text> §7Set comment on given step");
        chatMessage("§6color <Id> <Step|\"root\"> [Color] §7Set color on the given step");
        chatMessage("§6border <Id> <Step|\"root\"> [x1;y1;z1;x2;y2;z2] §7Change border");
        chatMessage("§6add-condition <Id> <Step|\"root\"> <Condition> §7Create a condition");
        chatMessage("§6delete-condition <Id> <Step|\"root\"> <Index> §7Delete the condition");
    }

    public static void processArgs(String[] args) {
        if (args.length <= 1) {
            return;
        }
        if ("toggle-display".equalsIgnoreCase(args[1])) {
            toggleDisplay();
        } else if ("identify-by".equalsIgnoreCase(args[1])) {
            if (args.length == 2) {
                chatMessage("§cPlease specify a room identification");
                return;
            }
            identifyBy(args[2]);
        } else if ("define-marker".equalsIgnoreCase(args[1])) {
            defineMarker();
        } else if ("list-secrets".equalsIgnoreCase(args[1])) {
            listSecrets();
        } else if ("new-secret".equalsIgnoreCase(args[1])) {
            newSecret();
        } else if ("delete-secret".equalsIgnoreCase(args[1])) {
            if (args.length != 3) {
                chatMessage("§cPlease specify a secret index");
                return;
            }
            try {
                deleteSecret(Integer.parseInt(args[2]));
            } catch (NumberFormatException ex) {
                chatMessage("§cGiven index is not a number");
            }
        } else if ("add-step".equalsIgnoreCase(args[1])) {
            if (args.length != 3) {
                chatMessage("§cPlease specify a secret index");
                return;
            }
            try {
                addStep(Integer.parseInt(args[2]));
            } catch (NumberFormatException ex) {
                chatMessage("§cGiven index is not a number");
            }
        } else if ("delete-step".equalsIgnoreCase(args[1])) {
            if (args.length != 4) {
                chatMessage("§cPlease specify a secret index and step index");
                return;
            }
            try {
                deleteStep(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("set-type".equalsIgnoreCase(args[1])) {
            if (args.length != 5) {
                chatMessage("§cPlease specify a secret index, a step index and a type");
                return;
            }
            try {
                setType(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), EnumUtils.getEnum(SecretType.class, args[4].toUpperCase()));
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("move".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                chatMessage("§cPlease specify a secret index and a step index");
                return;
            }
            try {
                move(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), (args.length >= 5 ? new RelativeLocation(args[4]) : null));
            } catch (DungeonSecretFinderException ex) {
                chatMessage("§cCoordinates are not properly formatted");
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("comment".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                chatMessage("§cPlease specify a secret index and a step index");
                return;
            }
            try {
                StringBuilder comment = new StringBuilder();
                for (int i = 4; i < args.length; i++) {
                    comment.append(args[i]).append(" ");
                }
                String commentStr = (comment.length() > 0 ? comment.substring(0, comment.length() - 1) : comment.toString());
                comment(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), commentStr);
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("color".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                chatMessage("§cPlease specify a secret index and a step index");
                return;
            }
            try {
                ColorData color = null;
                if (args.length >= 5) {
                    color = ColorMap.getColorData(args[4]);
                    if ("Undefined".equals(color.getName()) && args[4].matches("[0-9]+")) {
                        try {
                            color = ColorMap.getColorData(Integer.parseInt(args[4]));
                            if ("Undefined".equals(color.getName())) {
                                throw new DungeonSecretFinderException("Unknown colormap id");
                            }
                        } catch (Exception e) {
                            chatMessage("§cGiven colormap entry does not exist");
                            return;
                        }
                    }
                }
                if (color != null && "Undefined".equals(color.getName())) {
                    chatMessage("§cGiven colormap entry does not exist");
                    return;
                }
                color(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), color);
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("border".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                chatMessage("§cPlease specify a secret index and a step index");
                return;
            }
            try {
                border(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), (args.length >= 5 ? args[4] : null));
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("add-condition".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                chatMessage("§cPlease specify a secret index and a step index");
                return;
            }
            try {
                StringBuilder condition = new StringBuilder();
                for (int i = 4; i < args.length; i++) {
                    condition.append(args[i]).append(" ");
                }
                String conditionStr = (condition.length() > 0 ? condition.substring(0, condition.length() - 1) : condition.toString());
                addCondition(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), conditionStr);
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else if ("delete-condition".equalsIgnoreCase(args[1])) {
            if (args.length != 5) {
                chatMessage("§cPlease specify a secret index, a step index and a condition index");
                return;
            }
            try {
                deleteCondition(Integer.parseInt(args[2]), Integer.parseInt(args[3].replace("root", "-1")), Integer.parseInt(args[4]));
            } catch (NumberFormatException ex) {
                chatMessage("§cOne of the given indices is not a number");
            }
        } else {
            showHelp();
        }
    }

    private static void deleteCondition(int secretIndex, int stepIndex, int conditionIndex) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to delete a condition");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                try {
                    step.getConditions().remove(conditionIndex);
                    secretsFile.updateEntry(step);
                    chatMessage("§aDeleted a " + (stepIndex == -1 ? "root" : "step") + " condition");
                } catch (IndexOutOfBoundsException e) {
                    chatMessage("§cThere's no condition with the given index");
                }
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void addCondition(int secretIndex, int stepIndex, String conditionStr) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to add a condition");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                try {
                    JsonObject obj = new JsonParser().parse(conditionStr).getAsJsonObject();
                    SecretCondition condition = new SecretCondition(currentRoom, obj);
                    step.getConditions().add(condition);
                    secretsFile.updateEntry(step);
                    chatMessage("§aCreated a new " + (stepIndex == -1 ? "root" : "step") + " condition: " + obj.toString());
                } catch (DungeonSecretFinderException e) {
                    chatMessage("§cCondition is missing the type attribute");
                } catch (Exception e) {
                    chatMessage("§cCondition must be given in valid json object format");
                }
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void border(int secretIndex, int stepIndex, String border) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to change a border");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                AxisAlignedBB aabb = aabb(border);
                if (aabb == null) {
                    aabb = step.getSecretType().getDefaultBorderSize();
                }
                step.setBorderSize(aabb);
                secretsFile.updateEntry(step);
                chatMessage("§aUpdated border size of " + (stepIndex == -1 ? "root" : "step") + " to "
                    + aabb.minX + ", " + aabb.minY + ", " + aabb.minZ + ", " + aabb.maxX + ", " + aabb.maxY + ", " + aabb.maxZ);
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void color(int secretIndex, int stepIndex, ColorData colorData) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to change a color");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                ColorData data = (colorData != null ? colorData : step.getSecretType().getDefaultMarkerColor());
                step.setMarkerColor(data.getColor());
                secretsFile.updateEntry(step);
                chatMessage("§aUpdated color of " + (stepIndex == -1 ? "root" : "step") + " to " + data.getName());
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void comment(int secretIndex, int stepIndex, String comment) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room change a comment");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                step.setComment(comment);
                secretsFile.updateEntry(step);
                chatMessage("§aUpdated comment of " + (stepIndex == -1 ? "root" : "step") + " to \"" + comment + "\"");
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void move(int secretIndex, int stepIndex, RelativeLocation loc) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to move a location");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            EntityPlayerSP p = thePlayer();
            RelativeLocation location = (loc != null ? loc : new RelativeLocation(currentRoom.getCenterBlockPos(), new BlockPos(p.posX, p.posY, p.posZ))).rotate(currentRoom.getRotation());
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                step.setRelativeLocation(location);
                secretsFile.updateEntry(step);
                chatMessage("§aUpdated location of " + (stepIndex == -1 ? "root" : "step") + " to " + location.getX() + ", " + location.getY() + ", " + location.getZ());
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void setType(int secretIndex, int stepIndex, SecretType type) {
        if (type == null) {
            chatMessage("§cGiven type does not exist");
            return;
        }
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to set the type");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret step = getStep(secretIndex, stepIndex, rootSecrets);
                step.setSecretType(type);
                step.setBorderSize(type.getDefaultBorderSize());
                step.setMarkerColor(type.getDefaultMarkerColor().getColor());
                createConditionByType(currentRoom, step, type);
                secretsFile.updateEntry(step);
                chatMessage("§aUpdated type of " + (stepIndex == -1 ? "root" : "step") + " to " + type.name());
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static RoomSecret getStep(int secretIndex, int stepIndex, List<RoomSecret> rootSecrets) {
        RoomSecret step = rootSecrets.get(secretIndex);
        if (stepIndex != -1) {
            List<RoomSecret> secretChain = step.getSubordinateSecretChain();
            if (secretChain.size() > stepIndex) {
                step = secretChain.get(stepIndex);
            } else {
                chatMessage("§cGiven step does not exist");
            }
        }
        return step;
    }

    private static void deleteStep(int secretIndex, int stepIndex) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to delete a secret step");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > secretIndex) {
                RoomSecret secret = rootSecrets.get(secretIndex);
                List<RoomSecret> chain = secret.getSubordinateSecretChain();
                if (chain.size() > stepIndex) {
                    RoomSecret step = chain.get(stepIndex);
                    int deleteIndex = secretsFile.getIndex(step);
                    if (deleteIndex == -1) {
                        chatMessage("§cGiven step does not exist (file index is -1)");
                        return;
                    }
                    RoomSecret previous;
                    RoomSecret next = null;
                    if (stepIndex == 0) {
                        previous = secret;
                    } else {
                        previous = chain.get(stepIndex - 1);
                    }
                    if (stepIndex + 1 < chain.size()) {
                        next = chain.get(stepIndex + 1);
                    }
                    previous.setReferenced(next);
                    secretsFile.updateEntry(previous);
                    secretsFile.removeEntry(deleteIndex);
                    chatMessage("§aSecret step was deleted successfully");
                } else {
                    chatMessage("§cGiven step does not exist (out of bounds)");
                }
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void addStep(int index) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to create a secret step");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > index) {
                RoomSecret secret = rootSecrets.get(index);
                List<RoomSecret> chain = secret.getSubordinateSecretChain();
                if (!chain.isEmpty()) {
                    secret = chain.get(chain.size() - 1);
                }
                EntityPlayerSP p = thePlayer();
                RelativeLocation loc = new RelativeLocation(currentRoom.getCenterBlockPos(), new BlockPos(p.posX, p.posY, p.posZ)).rotate(currentRoom.getRotation());
                SecretType stepType = recognizeSecretType(p.worldObj, new BlockPos(p.posX, p.posY, p.posZ));
                RoomSecret newStep = new RoomSecret(UUID.randomUUID(), stepType, loc, "",
                    new ArrayList<>(), stepType.getDefaultBorderSize(), stepType.getDefaultMarkerColor().getColor(), null);
                createConditionByType(currentRoom, newStep, stepType);
                secretsFile.addEntry(newStep);
                secret.setReferenced(newStep);
                secretsFile.updateEntry(secret);
                chatMessage("§aA new step was created for the given secret");
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static SecretType recognizeSecretType(World w, BlockPos blockPos) {
        SecretType type = SecretType.LOCATION;
        if (w != null && blockPos != null) {
            for (int i = 0; i < 2; i++) {
                BlockPos bp = blockPos.subtract(new Vec3i(0, i, 0));
                Block block = w.getBlockState(bp).getBlock();
                if (block.equals(Blocks.chest) || block.equals(Blocks.trapped_chest)) {
                    type = SecretType.CHEST;
                    break;
                } else if (block.equals(Blocks.lever)) {
                    type = SecretType.LEVER;
                    break;
                } else if (block.equals(Blocks.stone_button) || block.equals(Blocks.wooden_button)) {
                    type = SecretType.BUTTON;
                    break;
                } else if (block.equals(Blocks.wooden_pressure_plate) || block.equals(Blocks.stone_pressure_plate)
                    || block.equals(Blocks.heavy_weighted_pressure_plate) || block.equals(Blocks.light_weighted_pressure_plate)
                    || block.equals(Blocks.skull)) {
                    type = SecretType.INTERACT;
                    break;
                } else if (block.equals(Blocks.stonebrick)) {
                    type = SecretType.DESTROY;
                    break;
                }
            }
        }
        if (type != SecretType.LOCATION) {
            chatMessage("§aDetected ideal secret type: §e" + type.name());
        }
        return type;
    }

    private static void deleteSecret(int index) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to delete the given secret");
        if (currentRoom != null) {
            if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
                chatMessage("§cPlease identify this room first");
                return;
            }
            RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
            List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
            if (rootSecrets.size() > index) {
                RoomSecret secret = rootSecrets.get(index);
                for (RoomSecret subordinate : secret.getSubordinateSecretChain()) {
                    secretsFile.removeEntry(secretsFile.getIndex(subordinate));
                }
                secretsFile.removeEntry(secretsFile.getIndex(secret));
                RoomDisplayContext.getInstance().resetOnChange();
                chatMessage("§aThe given secret was successfully deleted");
            } else {
                chatMessage("§cGiven secret does not exist");
            }
        }
    }

    private static void toggleDisplay() {
        RoomDisplayContext instance = RoomDisplayContext.getInstance();
        if (instance == null) {
            chatMessage("§cRoomDisplayContext not available");
            return;
        }
        if (instance.getDisplayOption() == DisplayOption.DISPLAY_ALL) {
            instance.setDisplayOption(DisplayOption.DISPLAY_MARKERS);
            chatMessage("§aNow showing §6room markers");
        } else {
            instance.setDisplayOption(DisplayOption.DISPLAY_ALL);
            chatMessage("§aNow showing §6room secrets");
        }
    }

    private static void identifyBy(String value) {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to set the identification");
        if (currentRoom == null) {
            return;
        }
        if (!value.matches("[a-zA-Z0-9\\-_]*")) {
            chatMessage("§cIdentification can only contain a-z, A-Z, 0-9 and -");
            return;
        }
        List<RoomIdentificationResolver> allResolvers = Arrays.stream(RoomType.values()).map(RoomIdentificationResolver::new).collect(Collectors.toList());
        if (allResolvers.stream().anyMatch(x -> x.getIdentifiers().stream().anyMatch(t -> t.getRoomId().equals(value)))) {
            chatMessage("§cThe given identification name is already in use");
            return;
        }
        RoomIdentification identification = currentRoom.getIdentification();
        if (identification != null) {
            RoomIdentificationFile file = new RoomIdentificationFile(currentRoom.getRoomType());
            if (file.exists()) {
                file.removeEntry(identification.getRoomId());
                RoomIdentification newIdentification = new RoomIdentification(value, identification.getMarkers());
                file.addEntry(newIdentification);
                ensureSecretsFile(currentRoom, newIdentification);
                chatMessage("§aSuccessfully changed rooms identification to §e" + value);
                return;
            }
        }
        RoomIdentification newIdentification = new RoomIdentification(value, identification != null ? identification.getMarkers() : new ArrayList<>());
        RoomIdentificationFile file = new RoomIdentificationFile(currentRoom.getRoomType());
        file.createFileIfNotExists();
        file.loadJsonObject();
        file.addEntry(newIdentification);
        ensureSecretsFile(currentRoom, newIdentification);
        chatMessage("§aSuccessfully created rooms identification §e" + value);
    }

    private static void defineMarker() {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to add/remove a marker");
        if (currentRoom == null) {
            return;
        }
        RoomIdentification identification = currentRoom.getIdentification();
        RoomIdentificationFile file = new RoomIdentificationFile(currentRoom.getRoomType());
        if (identification == null || !file.exists()) {
            chatMessage("§cPlease set an identification for this room first");
            return;
        }
        MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
        if (mouseOver.typeOfHit != MovingObjectType.BLOCK) {
            chatMessage("§cTo add/remove a marker you have to target a block");
            return;
        }
        BlockPos blockPos = mouseOver.getBlockPos();
        Block block = thePlayer().worldObj.getBlockState(blockPos).getBlock();
        Predicate<RoomIdentificationMarker> findExisting = x -> x.getLocation().equals(currentRoom.getCenterBlockPos(), blockPos);
        if (identification.getMarkers().stream().anyMatch(findExisting)) {
            file.removeEntry(identification.getRoomId());
            identification.getMarkers().removeIf(findExisting);
            file.addEntry(identification);
            chatMessage("§aRemoved marker " + block.getRegistryName() + " at " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
        } else {
            file.removeEntry(identification.getRoomId());
            identification.getMarkers().add(new RoomIdentificationMarker(block, new RelativeLocation(currentRoom.getCenterBlockPos(), blockPos).rotate(currentRoom.getRotation())));
            file.addEntry(identification);
            chatMessage("§aAdded marker " + block.getRegistryName() + " at " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
        }
    }

    private static void newSecret() {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to create a new secret");
        if (currentRoom == null) {
            return;
        }
        if (currentRoom.getIdentification() == null || currentRoom.getIdentification().getMarkers().isEmpty()) {
            chatMessage("§cPlease identify this room first");
            return;
        }
        EntityPlayerSP p = thePlayer();
        BlockPos playerPos = new BlockPos(p.posX, p.posY, p.posZ);
        SecretType secretType = recognizeSecretType(p.worldObj, playerPos);
        RoomSecret secret = new RoomSecret(UUID.randomUUID(), secretType,
            new RelativeLocation(currentRoom.getCenterBlockPos(), playerPos).rotate(currentRoom.getRotation()),
            "", new ArrayList<>(), secretType.getDefaultBorderSize(), secretType.getDefaultMarkerColor().getColor(), null);
        createConditionByType(currentRoom, secret, secretType);
        RoomSecretsFile secretsFile = new RoomSecretsFile(currentRoom);
        secretsFile.addEntry(secret);
        chatMessage("§aCreated a new secret in this room");
    }

    private static void ensureSecretsFile(DungeonRoom currentRoom, RoomIdentification newIdentification) {
        RoomSecretsFile secretsFile;
        JsonObject secretsObject;
        try {
            if (currentRoom.getIdentification() != null) {
                secretsFile = new RoomSecretsFile(currentRoom);
                secretsObject = secretsFile.getObject();
                if (secretsFile.exists()) {
                    secretsFile.delete();
                    secretsFile.close();
                }
            } else {
                secretsObject = new JsonObject();
            }
            currentRoom.setIdentification(newIdentification);
            if (secretsObject != null && !secretsObject.has("entries")) {
                secretsObject.add("entries", new JsonArray());
            }
            secretsFile = new RoomSecretsFile(currentRoom);
            secretsFile.createFileIfNotExists();
            secretsFile.loadJsonObject();
            secretsFile.setObject(secretsObject);
            secretsFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listSecrets() {
        DungeonRoom currentRoom = getCurrentRoom("§cPlease step inside a room to list the secrets of it");
        if (currentRoom != null) {
            List<RoomSecret> secrets = currentRoom.getRootSecrets();
            if (secrets.isEmpty()) {
                chatMessage("§cThis room does currently not have any secrets defined");
            } else {
                int index = 0;
                chatMessage("");
                chatMessage("§9§lEvery secret in this room:");
                for (RoomSecret secret : secrets) {
                    RelativeLocation rl = secret.getRelativeLocation();
                    List<RoomSecret> subordinates = secret.getSubordinateSecretChain();
                    chatMessage("§6" + (index++) + " §8| §a" + secret.getSecretType() + " §7" + rl.getX() + ", " + rl.getY() + ", " + rl.getZ()
                        + " §8[§e" + subordinates.size() + " Step" + (subordinates.size() == 1 ? "" : "s") + "§8]");
                    for (SecretCondition condition : secret.getConditions()) {
                        chatMessage(" §4⒞ §c" + condition.getType());
                        chatMessage(" §7" + condition.getConditionData().toString());
                    }
                    int subIndex = 0;
                    for (RoomSecret subordinate : subordinates) {
                        RelativeLocation loc = subordinate.getRelativeLocation();
                        chatMessage("  §8» §6" + (subIndex++) + " §8| §a" + subordinate.getSecretType() + " §7" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
                        for (SecretCondition condition : subordinate.getConditions()) {
                            chatMessage("   §4⒞ §c" + condition.getType());
                            chatMessage("   §7" + condition.getConditionData().toString());
                        }
                    }
                }
            }
        }
    }

    private static DungeonRoom getCurrentRoom(String errorMessage) {
        DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        DungeonRoom currentRoom = layout.getCurrentRoom();
        if (currentRoom == null) {
            chatMessage(errorMessage);
            return null;
        }
        return currentRoom;
    }

    private static AxisAlignedBB aabb(String str) {
        if (str == null) {
            return null;
        }
        String[] args = str.split(";");
        if (args.length != 6) {
            return null;
        }
        double x1 = Double.parseDouble(args[0]);
        double y1 = Double.parseDouble(args[1]);
        double z1 = Double.parseDouble(args[2]);
        double x2 = Double.parseDouble(args[3]);
        double y2 = Double.parseDouble(args[4]);
        double z2 = Double.parseDouble(args[5]);
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    private static EntityPlayerSP thePlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    private static void createConditionByType(DungeonRoom room, RoomSecret secret, SecretType type) {
        RelativeLocation l = secret.getRelativeLocation();
        World world = RuntimeContext.getInstance().getCurrentDungeonLayout().getApplicableWorld();
        Block block = world.getBlockState(l.toRealPosition(room)).getBlock();

        if ((type == SecretType.INTERACT && block == Blocks.skull) || (type == SecretType.DESTROY && block == Blocks.stonebrick)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("t", "BLOCK_EQUALS");
            obj.addProperty("r", l.getX() + ";" + l.getY() + ";" + l.getZ());
            obj.addProperty("v", block.getRegistryName());
            secret.getConditions().add(new SecretCondition(room, obj));
            chatMessage("§aDetected ideal secret condition:");
            chatMessage("§e" + obj.toString());
        }
    }

}
