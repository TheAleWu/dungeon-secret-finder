package de.alewu.dsf.commands;

import static de.alewu.dsf.util.Communication.chatMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alewu.dsf.DungeonSecretFinder;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.DungeonScanner;
import de.alewu.dsf.scanning.secrets.SecretType;
import de.alewu.dsf.util.ColorMap;
import de.alewu.dsf.util.RuntimeContext;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class MainModCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "dungeonsecretfinder";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("dsf");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp();
        } else {
            if (args[0].equalsIgnoreCase("uuid")) {
                UUID uuid = UUID.randomUUID();
                chatMessage("§6Generated uuid: §e" + uuid.toString());
                chatMessage("§7Also copied to clipboard");
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(uuid.toString()), null);
            } else if (args[0].equalsIgnoreCase("scan")) {
                CompletableFuture<DungeonLayout> scan = DungeonScanner.scan(null, null);
                if (scan == null) {
                    chatMessage("§cWorld could not be found");
                    return;
                }
                scan.whenCompleteAsync((results, exception) -> {
                    if (exception != null) {
                        chatMessage("§cAn error occurred when scanning. Check console for more infos");
                        exception.printStackTrace();
                    } else {
                        chatMessage("§aScan success. Check console for results!");
                    }
                });
            } else if (args[0].equalsIgnoreCase("scanignore")) {
                EntityPlayerSP mc = Minecraft.getMinecraft().thePlayer;
                CompletableFuture<DungeonLayout> scan = DungeonScanner.scan((int) mc.posX, (int) mc.posZ);
                if (scan == null) {
                    chatMessage("§cWorld could not be found");
                    return;
                }
                scan.whenCompleteAsync((results, exception) -> {
                    if (exception != null) {
                        chatMessage("§cAn error occurred when scanning. Check console for more infos");
                        exception.printStackTrace();
                    } else {
                        chatMessage("§aScan success. Check console for results!");
                    }
                });
            } else if (args[0].equalsIgnoreCase("clear")) {
                if (RuntimeContext.getInstance().getCurrentDungeonLayout() == null) {
                    chatMessage("§cNo dungeon layout has been scanned");
                    return;
                }
                RuntimeContext.getInstance().setCurrentDungeonLayout(null);
                chatMessage("§aThe dungeon layout has been cleared");
            } else if (args[0].equalsIgnoreCase("preparedata")) {
                prepareData();
            } else if (args[0].equalsIgnoreCase("debugtimer")) {
                DungeonSecretFinder.debug();
            } else if (args[0].equalsIgnoreCase("relative") || args[0].equalsIgnoreCase("rel")) {
                MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
                if (mouseOver.typeOfHit == MovingObjectType.BLOCK) {
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                    World w = thePlayer.worldObj;
                    if (w == null) {
                        chatMessage("§cUnable to get marker data: current world is null");
                        return;
                    }
                    BlockPos blockPos = mouseOver.getBlockPos();
                    if (blockPos != null) {
                        DungeonLayout dungeonLayout = RuntimeContext.getInstance().getCurrentDungeonLayout();
                        if (dungeonLayout == null) {
                            chatMessage("§cThe dungeon layout has not yet been identified");
                            return;
                        }
                        DungeonRoom currentRoom = dungeonLayout.getCurrentRoom();
                        if (currentRoom == null) {
                            chatMessage("§cYou are not inside of a dungeon room");
                            return;
                        }
                        Triple<Float, Float, Float> centerPosition = currentRoom.getCenterPosition();
                        if (centerPosition == null) {
                            chatMessage("§cThe current dungeon room does not have a center position");
                            return;
                        }
                        float diffX = blockPos.getX() - centerPosition.getLeft() + 0.5f;
                        float diffY = blockPos.getY() - centerPosition.getMiddle();
                        float diffZ = blockPos.getZ() - centerPosition.getRight() + 0.5f;
                        chatMessage("§aThe following marker data has been calculated:");
                        String blockName = w.getBlockState(blockPos).getBlock().getRegistryName();
                        chatMessage("§6Block: §e" + blockName);
                        chatMessage("§6Relative Location: §e" + diffX + ";" + diffY + ";" + diffZ);
                        if (args.length == 2) {
                            switch (args[1].toLowerCase()) {
                                case "-clipboard": {
                                    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    systemClipboard.setContents(new StringSelection("{\n\t\"block\": \"" + blockName + "\",\n\t\"relative\": \"" + diffX + ";" + diffY + ";" + diffZ + "\"\n}"), null);
                                    chatMessage("§2Data has been copied to your clipboard.");
                                    break;
                                }
                                case "-nlb-clipboard": {
                                    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    systemClipboard.setContents(new StringSelection("{\"block\": \"" + blockName + "\",\"relative\": \"" + diffX + ";" + diffY + ";" + diffZ + "\"}"), null);
                                    chatMessage("§2Data has been copied to your clipboard");
                                    break;
                                }
                                case "-blank-clipboard": {
                                    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    systemClipboard.setContents(new StringSelection(diffX + ";" + diffY + ";" + diffZ), null);
                                    chatMessage("§2Data has been copied to your clipboard");
                                    break;
                                }
                            }
                        }
                    } else {
                        chatMessage("§cYou need to target a block to get the marker data");
                    }
                } else {
                    chatMessage("§cYou need to target a block to get the marker data");
                }
            } else if (args[0].equalsIgnoreCase("colormap") || args[0].equalsIgnoreCase("cm")) {
                chatMessage("");
                chatMessage("§6§lAvailable color map:");
                chatMessage("§8> §7Id  §8|§7  Name  §8|§7  Color value");
                ColorMap.getColorMap().forEach((id, color) -> {
                    String name = color.getName();
                    String colorValue = color.getColor().getRed() + " " + color.getColor().getGreen() + " " + color.getColor().getBlue();
                    chatMessage("§8> §e" + id + "  §8|§e  " + name + "  §8|§e  " + colorValue);
                });
            } else if (args[0].equalsIgnoreCase("secrettypes") || args[0].equalsIgnoreCase("st")) {
                chatMessage("");
                chatMessage("§6§lAvailable secret types:");
                chatMessage("§8> §7Id  §8|§7  Default Color  §8|§7  Default border");
                Arrays.asList(SecretType.values()).forEach(s -> {
                    String color = s.getDefaultMarkerColor().getName();
                    AxisAlignedBB b = s.getDefaultBorderSize();
                    String border = b.minX + ", " + b.minY + ", " + b.minZ + ", " + b.maxX + ", " + b.maxY + ", " + b.maxZ;
                    chatMessage("§8> §e" + s.name() + "  §8|§e  " + color + "  §8|§e  " + border);
                });
            } else if (args[0].equalsIgnoreCase("roomdesigner") || args[0].equalsIgnoreCase("rd")) {
                DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
                if (layout == null) {
                    chatMessage("§cPlease scan the dungeon first before using the room designer.");
                    return;
                }
                if (args.length == 1) {
                    RoomDesignerLogic.showHelp();
                } else {
                    RoomDesignerLogic.processArgs(args);
                }
            }
        }
    }

    private void prepareData() {
        File modDirectory = new File("dsf");
        File outputDirectory = new File(modDirectory, "output");
        File identifiersOutput = new File(outputDirectory, "identifiers.json");
        File secretsOutput = new File(outputDirectory, "secrets.json");
        File assetsVersionOutput = new File(outputDirectory, "assets_version.json");
        if (!modDirectory.exists()) {
            boolean created = modDirectory.mkdirs();
            if (!created) {
                chatMessage("§cCould not create mod directory");
                return;
            }
        }
        if (!outputDirectory.exists()) {
            boolean created = outputDirectory.mkdirs();
            if (!created) {
                chatMessage("§cCould not create output directory");
                return;
            }
        }
        int version;
        JsonParser jsonParser = new JsonParser();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/assets/dsf/assets_version.json")))) {
            if (!identifiersOutput.exists()) {
                boolean created = identifiersOutput.createNewFile();
                if (!created) {
                    chatMessage("§cCould not create identifiers output file");
                    return;
                }
            }
            if (!secretsOutput.exists()) {
                boolean created = secretsOutput.createNewFile();
                if (!created) {
                    chatMessage("§cCould not create secrets output file");
                    return;
                }
            }

            StringBuilder builder = new StringBuilder();
            while (reader.ready()) {
                builder.append(reader.readLine());
            }
            JsonObject obj = (JsonObject) jsonParser.parse(builder.toString());
            if (!obj.has("version")) {
                chatMessage("§cVersion file is missing version attribute");
                return;
            }
            version = obj.get("version").getAsInt();
            if (version == -1) {
                chatMessage("§cVersion could not get updated");
                return;
            }

            File identifiersDirectory = new File(modDirectory, "/identification");
            if (identifiersDirectory.exists()) {
                JsonObject identifiersObject = new JsonObject();
                for (File f : Objects.requireNonNull(identifiersDirectory.listFiles())) {
                    JsonObject identifierObject = (JsonObject) jsonParser.parse(new InputStreamReader(new FileInputStream(f)));
                    identifiersObject.add(f.getName().replaceAll("\\.json", ""), identifierObject);
                }
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(identifiersOutput)))) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    gson.toJson(identifiersObject, writer);
                    writer.flush();
                }
            }

            File secretsDirectory = new File(modDirectory, "/secrets");
            if (secretsDirectory.exists()) {
                JsonObject secretsObject = new JsonObject();
                for (File f : Objects.requireNonNull(secretsDirectory.listFiles())) {
                    JsonObject secretObject = (JsonObject) jsonParser.parse(new InputStreamReader(new FileInputStream(f)));
                    secretsObject.add(f.getName().replaceAll("\\.json", ""), secretObject);
                }
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(secretsOutput)))) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    gson.toJson(secretsObject, writer);
                    writer.flush();
                }
            }
            JsonObject assetsVersionObject = new JsonObject();
            assetsVersionObject.addProperty("version", ++version);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(assetsVersionOutput)))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                gson.toJson(assetsVersionObject, writer);
                writer.flush();
            }
            chatMessage("§aCreated output data in output folder");
        } catch (Exception e) {
            chatMessage("§cAn error has occurred. Check logs for more information");
            e.printStackTrace();
        }
    }

    private void sendHelp() {
        chatMessage("");
        chatMessage("§5§lDungeonSecretFinder §7Help:");
        chatMessage("§6/dsf scan §7Scan dungeon");
        chatMessage("§6/dsf clear §7Clear previous scan");
        chatMessage("§6/dsf preparedata §7Prepare data for github");
        chatMessage("§6/dsf relative | rel §7Prints relative location");
        chatMessage("   §e-clipboard §7Also copies to clipboard");
        chatMessage("   §e-nlb-clipboard §7Also copies to clipboard without linebreaks");
        chatMessage("   §e-blank-clipboard §7Also copies blank coords to clipboard");
        chatMessage("§6/dsf debugmarker | dm §7Shows debug marker");
        chatMessage("   §e-c$<colormap-id> §7Sets the color");
        chatMessage("   §e-s$<x1>;<y1>;<z1>;<x2>;<y2>;<z2> §7Sets the size");
        chatMessage("   §e-p$<cursor/feet> §7Sets the position");
        chatMessage("   §e-t$<x>;<y>;<z> §7Sets the location shift");
        chatMessage("   §e-i §7Shows all the debug marker data");
        chatMessage("§6/dsf colormap | cm §7Shows the available colormap");
        chatMessage("§6/dsf secrettypes | st §7Shows the available secret types");
        chatMessage("§6/dsf roomdesigner | rd §7Show room designer help");
    }


}
