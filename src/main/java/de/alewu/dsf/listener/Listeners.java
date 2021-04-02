package de.alewu.dsf.listener;

import static de.alewu.dsf.util.Communication.chatMessage;
import static de.alewu.dsf.util.RenderUtils.drawBoundingBox;
import static de.alewu.dsf.util.RenderUtils.drawDebugMarker;

import de.alewu.dsf.DungeonSecretFinder;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.DungeonScanner;
import de.alewu.dsf.scanning.identification.RoomIdentification;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import de.alewu.dsf.util.KeyBindingUtil;
import de.alewu.dsf.util.RoomDisplayContext;
import de.alewu.dsf.util.RoomDisplayContext.DisplayOption;
import de.alewu.dsf.util.RuntimeContext;
import de.alewu.dsf.util.ServerConnection;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;

public class Listeners {

    private static final AxisAlignedBB DEBUG_MARKER_BORDER = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent e) {
        if (e.type == 0 && e.message.getUnformattedText().equals("§e[NPC] §bMort§f: Talk to me to change your class and ready up.")) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            final int playerX = (int) thePlayer.posX;
            final int playerZ = (int) thePlayer.posZ;
            CompletableFuture<DungeonLayout> scan = DungeonScanner.scan(playerX, playerZ);
            if (scan == null) {
                chatMessage("§5§lDSF: §cThe world could not be recognized...");
                chatMessage("§5§lDSF: §cTo manually scan use §e/dsf scan");
                return;
            }
            scan.whenCompleteAsync((results, exception) -> {
                if (exception != null) {
                    chatMessage("§5§lDSF: §cAn error occurred when scanning. Check the logs for more infos");
                    chatMessage("§5§lDSF: §cTo manually scan use §e/dsf scan");
                    exception.printStackTrace();
                } else {
                    chatMessage("§5§lDSF: §aDungeon scanning completed successfully.");
                }
            });
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent e) {
        if (e.type == Type.CLIENT && e.phase == Phase.START) {
            if (RuntimeContext.getInstance().isDebugDisabled() && ServerConnection.isInvalid()) {
                RuntimeContext runtimeContext = RuntimeContext.getInstance();
                if (runtimeContext.getCurrentDungeonLayout() != null) {
                    runtimeContext.setCurrentDungeonLayout(null);
                }
                RoomDisplayContext roomDisplayContext = RoomDisplayContext.getInstance();
                if (roomDisplayContext != null) {
                    roomDisplayContext.purge();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        try {
            drawDebugMarker();

            if (RuntimeContext.getInstance().isDebugDisabled() && ServerConnection.isInvalid()) {
                return;
            }
            DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
            if (layout == null || layout.getCurrentRoom() == null) {
                return;
            }
            DungeonRoom room = layout.getCurrentRoom();
            if (RuntimeContext.getInstance().isDebugDisplayingMarkers()) {
                displayMarkers(room);
            } else {
                RoomDisplayContext ctx = RoomDisplayContext.getInstance();
                if (ctx.getDisplayOption() == DisplayOption.DISPLAY_MARKERS) {
                    displayMarkers(room);
                } else if (ctx.getDisplayOption() == DisplayOption.DISPLAY_ALL) {
                    room.getRootSecrets().forEach(secret -> drawSecret(room, secret));
                } else if (ctx.getDisplayOption() == DisplayOption.DISPLAY_SINGLE) {
                    int index = ctx.getDisplayedSecretIndex();
                    if (index == -1 || room.getRootSecrets().isEmpty()) {
                        return;
                    }
                    try {
                        RoomSecret secret = room.getRootSecrets().get(index);
                        drawSecret(room, secret);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ctx.purge();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayMarkers(DungeonRoom room) {
        RoomIdentification identification = room.getIdentification();
        if (identification != null) {
            identification.getMarkers().forEach(marker -> {
                GlStateManager.pushMatrix();

                RelativeLocation loc = marker.getLocation().rotate(room.getRotation());
                BlockPos c = room.getCenterBlockPos();
                drawBoundingBox(Triple.of(c.getX() + loc.getX(), c.getY() + loc.getY(), c.getZ() + loc.getZ()), DEBUG_MARKER_BORDER, Color.RED);

                GlStateManager.resetColor();
                GlStateManager.popMatrix();
            });
        }
    }

    private void drawSecret(DungeonRoom room, RoomSecret secret) {
        GlStateManager.pushMatrix();

        RelativeLocation loc = secret.getRelativeLocation().rotate(room.getRotation());
        BlockPos c = room.getCenterBlockPos();
        // Draw secret
        drawBoundingBox(Triple.of(c.getX() + loc.getX(), c.getY() + loc.getY(), c.getZ() + loc.getZ()), secret.getBorderSize(), secret.getMarkerColor());

        GlStateManager.resetColor();
        GlStateManager.popMatrix();

        // Draw steps of secret
        secret.getSubordinateSecretChain().forEach(s -> {
            GlStateManager.pushMatrix();
            RelativeLocation l = s.getRelativeLocation().rotate(room.getRotation());
            BlockPos center = room.getCenterBlockPos();
            AxisAlignedBB border = /*new AxisAlignedBB(0.4, 0.4, 0.4, 0.6, 0.6, 0.6)*/ s.getBorderSize();
            drawBoundingBox(Triple.of(center.getX() + l.getX(), center.getY() + l.getY(), center.getZ() + l.getZ()), border, s.getMarkerColor());
            GlStateManager.resetColor();
            GlStateManager.popMatrix();
        });
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent e) {
        if (RuntimeContext.getInstance().isDebugDisabled() && ServerConnection.isInvalid()) {
            return;
        }
        RoomDisplayContext displayData = RoomDisplayContext.getInstance();
        if (displayData == null) {
            return;
        }
        if (KeyBindingUtil.KEY_BINDING_SECRET_BACK.isPressed() && displayData.getDisplayOption() == DisplayOption.DISPLAY_SINGLE) {
            displayData.previousSecretIndex();
        }
        if (KeyBindingUtil.KEY_BINDING_SECRET_FORWARD.isPressed() && displayData.getDisplayOption() == DisplayOption.DISPLAY_SINGLE) {
            displayData.nextSecretIndex();
        }
        if (KeyBindingUtil.KEY_BINDING_SECRET_DISPLAY_BACK.isPressed()) {
            displayData.previousDisplayOption();
        }
        if (KeyBindingUtil.KEY_BINDING_SECRET_DISPLAY_FORWARD.isPressed()) {
            displayData.nextDisplayOption();
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != ElementType.EXPERIENCE) {
            return;
        }
        if (RuntimeContext.getInstance().isDebugDisabled() && ServerConnection.isInvalid()) {
            return;
        }
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) {
            return;
        }
        DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        if (layout == null) {
            return;
        }
        DungeonRoom currentRoom = layout.getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        Triple<Float, Float, Float> centerPosition = currentRoom.getCenterPosition();
        if (centerPosition == null) {
            return;
        }
        printSecretInfo(fontRenderer, currentRoom);
    }

    private void printSecretInfo(FontRenderer fontRenderer, DungeonRoom currentRoom) {
        RoomDisplayContext ctx = RoomDisplayContext.getInstance();
        if (ctx.hasRoomChanged(currentRoom)) {
            ctx.setRoom(currentRoom);
        }

        List<RoomSecret> rootSecrets = currentRoom.getRootSecrets();
        List<String> data = new ArrayList<>();
        data.add("§9§lDebug information:");
        data.add("§6Room rotation: §e" + (currentRoom.getRotation() != null ?
            (currentRoom.getRotation() + " (" + currentRoom.getRotation().getRotationAmount() + ")") : "Unknown (?)"));
        data.add("§6Room identification: §e" + (currentRoom.getIdentification() != null ? currentRoom.getIdentification().getRoomId() : "None"));
        data.add("§6Identification file: §e" + (currentRoom.getNormalizedRoomType() != null ? currentRoom.getNormalizedRoomType().getMarkersFileName() : "?"));
        data.add("§6Secrets file: §e" + (currentRoom.getIdentification() != null ? currentRoom.getIdentification().getRoomId() + ".json" : "-"));
        data.add("");
        if (ctx.getDisplayOption() == DisplayOption.DISPLAY_MARKERS) {
            data.add("§aDisplaying all room markers");
        } else if (ctx.getDisplayOption() == DisplayOption.DISPLAY_ALL) {
            int index = 1;
            if (!rootSecrets.isEmpty()) {
                data.add("§2Displaying all secrets");
                for (RoomSecret secret : rootSecrets) {
                    String comment = (org.apache.commons.lang3.StringUtils.isNotBlank(secret.getComment())
                        ? secret.getComment()
                        : secret.getSecretType().getDefaultComment());
                    BlockPos pos = secret.getRelativeLocation().toRealPosition(currentRoom);
                    data.add("§6#" + (index++) + " §e» §a" + comment + " §8at §7" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                }
            } else {
                data.add("§c§nDisplaying all secrets");
                data.add("§cThis room contains no secrets");
            }
        } else if (ctx.getDisplayOption() == DisplayOption.DISPLAY_SINGLE) {
            int index = ctx.getDisplayedSecretIndex();
            if (index != -1 && !rootSecrets.isEmpty()) {
                RoomSecret secret = rootSecrets.get(index);
                data.add("§2Displaying Secret #" + (index + 1) + " of " + rootSecrets.size());
                if (secret.getSubordinateSecretChain().isEmpty()) {
                    String comment = (org.apache.commons.lang3.StringUtils.isNotBlank(secret.getComment())
                        ? secret.getComment()
                        : secret.getSecretType().getDefaultComment());
                    BlockPos pos = secret.getRelativeLocation().toRealPosition(currentRoom);
                    data.add("§6#1 §e» §a" + comment + " §8at §7" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                } else {
                    int step = 1;
                    String comment = (org.apache.commons.lang3.StringUtils.isNotBlank(secret.getComment())
                        ? secret.getComment()
                        : secret.getSecretType().getDefaultComment());
                    BlockPos pos = secret.getRelativeLocation().toRealPosition(currentRoom);
                    data.add("§6#" + (step++) + " §e» §a" + comment + " §8at §7" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                    for (RoomSecret subordinate : secret.getSubordinateSecretChain()) {
                        comment = (org.apache.commons.lang3.StringUtils.isNotBlank(subordinate.getComment())
                            ? subordinate.getComment()
                            : subordinate.getSecretType().getDefaultComment());
                        pos = subordinate.getRelativeLocation().toRealPosition(currentRoom);
                        data.add("§6#" + (step++) + " §e» §a" + comment + " §8at §7" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                    }
                }
            } else {
                data.add("§c§nDisplaying secret chains");
                data.add("§cThis room contains no secrets");
            }
        } else if (ctx.getDisplayOption() == DisplayOption.DISPLAY_NONE) {
            String backName = Keyboard.getKeyName(KeyBindingUtil.KEY_BINDING_SECRET_DISPLAY_BACK.getKeyCode());
            String forwardName = Keyboard.getKeyName(KeyBindingUtil.KEY_BINDING_SECRET_DISPLAY_FORWARD.getKeyCode());
            data.add("§cSecret markers and their data are hidden!");
            data.add("§7Press §e" + backName + " §7or §e" + forwardName + " §7to toggle the display settings.");
        }
        String versionInfo = "§5Using DungeonSecretFinder " + DungeonSecretFinder.VERSION + " by AleWu";
        data.add(versionInfo);

        GlStateManager.pushMatrix();
        float scaling = 1f;
        GlStateManager.scale(scaling, scaling, 1);

        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        int versionInfoTextLength = fontRenderer.getStringWidth(versionInfo);
        int height = res.getScaledHeight() - fontRenderer.FONT_HEIGHT * data.size() - fontRenderer.FONT_HEIGHT - 3;
        int longestString = data.stream()
            .map(x -> fontRenderer.getStringWidth(StringUtils.stripControlCodes(x))).max(Comparator.comparingInt(x -> x))
            .orElse(versionInfoTextLength);
        for (int i = 0; i < data.size(); i++) {
            String s = data.get(i);
            fontRenderer.drawString(s,
                (int) (res.getScaledWidth() / scaling) - longestString - 100,
                (int) (height / scaling),
                Color.WHITE.getRGB());
            height += fontRenderer.FONT_HEIGHT;
            if (i + 1 == data.size() - 1) {
                height += fontRenderer.FONT_HEIGHT;
            }
        }
        GlStateManager.popMatrix();
    }

}
