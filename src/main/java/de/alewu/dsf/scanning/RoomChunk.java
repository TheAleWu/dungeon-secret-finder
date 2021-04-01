package de.alewu.dsf.scanning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomChunk {

    static final int CHUNK_SIZE_X = 32;
    static final int CHUNK_SIZE_Z = 32;
    private final UUID roomId = UUID.randomUUID();
    private final int chunkX;
    private final int chunkZ;
    private final List<RoomChunk> childChunks;
    private RoomChunk rootChunk = null;

    public RoomChunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.childChunks = new ArrayList<>();
    }

    public UUID getRoomId() {
        return roomId;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getRealX() {
        return chunkX * CHUNK_SIZE_X;
    }

    public int getRealSizeX() {
        return getRealX() + 30;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getRealZ() {
        return chunkZ * CHUNK_SIZE_Z;
    }

    public int getRealSizeZ() {
        return getRealZ() + 30;
    }

    public void setRootChunk(RoomChunk rootChunk) {
        this.rootChunk = rootChunk;
    }

    public RoomChunk getRootChunk() {
        return rootChunk;
    }

    public List<RoomChunk> getChildChunks() {
        return childChunks;
    }

    public boolean isInBoundaries(int x, int z) {
        return getRealX() <= x && getRealX() + CHUNK_SIZE_X - 1 >= x
            && getRealZ() <= z && getRealZ() + CHUNK_SIZE_Z - 1 >= z;
    }
}
