package de.alewu.dsf.scanning;

import de.alewu.dsf.scanning.identification.RoomIdentification;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import de.alewu.dsf.scanning.shared.RoomRotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

public class DungeonRoom {

    private final RoomChunk anchor;
    private final List<RoomChunk> roomChunks = new ArrayList<>();
    private RoomRotation rotation;
    private Triple<Float, Float, Float> centerPosition;
    private RoomIdentification identification;
    private List<RoomSecret> secrets = new ArrayList<>();
    RoomType roomType = RoomType.UNRECOGNIZABLE;
    RoomType normalizedRoomType;

    public DungeonRoom(RoomChunk anchor) {
        this.anchor = anchor;
    }

    public void addRoomChunk(RoomChunk chunk) {
        if (roomChunks.stream().anyMatch(x -> x.getRoomId().equals(chunk.getRoomId())
            || (x.getChunkX() == chunk.getChunkX() && x.getChunkZ() == chunk.getChunkZ()))) {
            return;
        }
        roomChunks.add(chunk);
        roomChunks.sort(Comparator.comparingInt(RoomChunk::getChunkX));
    }

    void reevaluateRoomType() {
        RoomType type = RoomType.UNRECOGNIZABLE;
        boolean[][] grid = new boolean[6][6];
        for (RoomChunk rc : roomChunks) {
            if (rc.getChunkX() > 6 || rc.getChunkX() < 0 || rc.getChunkZ() > 6 || rc.getChunkZ() < 0) {
                continue;
            }
            grid[rc.getChunkX()][rc.getChunkZ()] = true;
        }
        Comparator<RoomType> checkOrder = Comparator.comparingInt(x -> x.getCheckedPositions().size());
        List<RoomType> checkAgainst = Arrays.stream(RoomType.values())
            .filter(x -> x.getCheckedPositions().size() == roomChunks.size())
            .sorted(checkOrder)
            .collect(Collectors.toList());
        if (!checkAgainst.isEmpty()) {
            for (int x = 0; x < grid.length; x++) {
                for (int z = 0; z < grid[x].length; z++) {
                    for (RoomType rt : checkAgainst) {
                        try {
                            int finalX = x;
                            int finalZ = z;
                            if (rt.getCheckedPositions().stream().allMatch(cp -> grid[finalX + cp.getLeft()][finalZ + cp.getRight()])) {
                                this.roomType = rt;
                                return;
                            }
                        } catch (ArrayIndexOutOfBoundsException ignored) {
                            // Outside grid - continue
                        }
                    }
                }
            }
        }
        this.roomType = type;
    }

    public RoomChunk getAnchor() {
        return anchor;
    }

    public List<RoomChunk> getRoomChunks() {
        return roomChunks;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public RoomType getNormalizedRoomType() {
        return normalizedRoomType;
    }

    public RoomRotation getRotation() {
        return rotation;
    }

    public void setRotation(RoomRotation rotation) {
        this.rotation = rotation;
    }

    public void setIdentification(RoomIdentification identification) {
        this.identification = identification;
    }

    public RoomIdentification getIdentification() {
        return identification;
    }

    public void setSecrets(List<RoomSecret> secrets) {
        this.secrets = secrets;
    }

    public List<RoomSecret> getSecrets() {
        return secrets;
    }

    public List<RoomSecret> getRootSecrets() {
        return secrets.stream().filter(x -> x.isRootSecret(secrets)).collect(Collectors.toList());
    }

    public void updateCenterPosition() {
        int minX = roomChunks.stream().min(Comparator.comparingInt(RoomChunk::getRealX)).orElse(new RoomChunk(-1, -1)).getRealX();
        int minZ = roomChunks.stream().min(Comparator.comparingInt(RoomChunk::getRealZ)).orElse(new RoomChunk(-1, -1)).getRealZ();

        int x = minX + roomType.getDeltaToCenter().getLeft();
        int z = minZ + roomType.getDeltaToCenter().getRight();

        centerPosition = Triple.of(x + 0.5f, 64f, z + 0.5f);
    }

    public Triple<Float, Float, Float> getCenterPosition() {
        return centerPosition;
    }

    public BlockPos getCenterBlockPos() {
        if (centerPosition == null) {
            return new BlockPos(0, 0, 0);
        }
        return new BlockPos(centerPosition.getLeft(), centerPosition.getMiddle(), centerPosition.getRight());
    }
}
