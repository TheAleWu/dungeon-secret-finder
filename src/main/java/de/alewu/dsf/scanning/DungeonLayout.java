package de.alewu.dsf.scanning;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class DungeonLayout {

    private final List<DungeonRoom> rooms = new ArrayList<>();
    private final List<RoomChunk> chunks = new ArrayList<>();
    private final World applicableWorld;
    private final Integer ignoredX;
    private final Integer ignoredZ;

    DungeonLayout(World applicableWorld, Integer ignoredX, Integer ignoredZ) {
        this.applicableWorld = applicableWorld;
        this.ignoredX = ignoredX;
        this.ignoredZ = ignoredZ;
    }

    public void addChunk(RoomChunk chunk) {
        if (chunks.stream().anyMatch(x -> x.getRoomId().equals(chunk.getRoomId()))) {
            return;
        }
        chunks.add(chunk);
    }

    public void addRoom(DungeonRoom room) {
        if (rooms.stream().anyMatch(x -> x.getAnchor().getRoomId().equals(room.getAnchor().getRoomId()))) {
            return;
        }
        rooms.add(room);
    }

    public RoomChunk getChunk(int realX, int realZ) {
        return chunks.stream()
            .filter(x -> x.getChunkX() == realX / RoomChunk.CHUNK_SIZE_X && x.getChunkZ() == realZ / RoomChunk.CHUNK_SIZE_Z)
            .findFirst()
            .orElse(null);
    }

    public DungeonRoom getRoom(int realX, int realZ) {
        RoomChunk chunk = getChunk(realX, realZ);
        if (chunk == null) {
            return null;
        }
        return rooms.stream()
            .filter(x -> x.getRoomChunks().stream().anyMatch(y -> y.getRoomId().equals(chunk.getRoomId())))
            .findFirst()
            .orElse(null);
    }

    public DungeonRoom getRoom(String identification) {
        return rooms.stream()
            .filter(x -> x.getIdentification() != null && x.getIdentification().getRoomId().equals(identification))
            .findFirst()
            .orElse(null);
    }

    public List<DungeonRoom> getRooms() {
        return ImmutableList.copyOf(rooms);
    }

    public List<RoomChunk> getChunks() {
        return ImmutableList.copyOf(chunks);
    }

    public World getApplicableWorld() {
        return applicableWorld;
    }

    public DungeonRoom getCurrentRoom() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return null;
        }
        return getRoom((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
    }

    public Integer getIgnoredX() {
        return ignoredX;
    }

    public Integer getIgnoredZ() {
        return ignoredZ;
    }
}
