package de.alewu.dsf.scanning;

import de.alewu.dsf.exceptions.DungeonScannerException;
import de.alewu.dsf.exceptions.DungeonSecretFinderException;
import de.alewu.dsf.scanning.identification.RoomIdentification;
import de.alewu.dsf.scanning.resolvers.RoomIdentificationResolver;
import de.alewu.dsf.scanning.resolvers.RoomSecretsResolver;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import de.alewu.dsf.scanning.shared.RoomRotation;
import de.alewu.dsf.util.Communication;
import de.alewu.dsf.util.RuntimeContext;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class DungeonScanner {

    private static final Logger LOG = Logger.getLogger(DungeonScanner.class.getName());
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);
    private static final int X_ADJUSTMENT = 0;
    private static final int Z_ADJUSTMENT = 0;
    private static final int SCAN_SIZE_X = 6;
    private static final int SCAN_SIZE_Z = 6;

    public static CompletableFuture<DungeonLayout> scan(Integer ignoredX, Integer ignoredZ) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        World w = thePlayer.worldObj;
        if (w == null) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                DungeonLayout layout = new DungeonLayout(w, ignoredX, ignoredZ);

                // Scan rooms on itself
                for (int chunkZ = -Z_ADJUSTMENT; chunkZ < SCAN_SIZE_Z; chunkZ++) {
                    for (int chunkX = -X_ADJUSTMENT; chunkX < SCAN_SIZE_X; chunkX++) {
                        int x = chunkX * 32;
                        int z = chunkZ * 32;
                        int y = findHighestYAt(w, x, z);
                        if (y > 0) {
                            RoomChunk chunk = new RoomChunk(chunkX, chunkZ);
                            layout.addChunk(chunk);
                        }
                    }
                }

                // Connect rooms together
                for (RoomChunk chunk : layout.getChunks()) {
                    if (ignoredX != null && ignoredZ != null && chunk.isInBoundaries(ignoredX, ignoredZ)) {
                        continue;
                    }
                    int x = chunk.getRealX();
                    int z = chunk.getRealZ();
                    Pair<Integer, Integer> bottomRight = Pair.of(x + 30, z + 30);
                    BlockPos right = new BlockPos(bottomRight.getLeft() + 1,
                        Math.min(
                            findHighestYAt(w, bottomRight.getLeft(), bottomRight.getRight()),
                            findHighestYAt(w, bottomRight.getLeft() + 2, bottomRight.getRight())),
                        bottomRight.getRight());
                    if (w.getBlockState(right).getBlock() != Blocks.air) { // Right
                        connectChunks(layout, chunk, t -> t.isInBoundaries(bottomRight.getLeft() + 2, bottomRight.getRight()));
                    }
                    BlockPos bottom = new BlockPos(bottomRight.getLeft(),
                        Math.min(
                            findHighestYAt(w, bottomRight.getLeft(), bottomRight.getRight()),
                            findHighestYAt(w, bottomRight.getLeft(), bottomRight.getRight() + 2)),
                        bottomRight.getRight() + 1);
                    if (w.getBlockState(bottom).getBlock() != Blocks.air) { // Bottom
                        connectChunks(layout, chunk, t -> t.isInBoundaries(bottomRight.getLeft(), bottomRight.getRight() + 2));
                    }
                }

                List<RoomChunk> rootChunks = layout.getChunks().stream().filter(c -> c.getRootChunk() == null).collect(Collectors.toList());
                for (RoomChunk c : rootChunks) {
                    List<RoomChunk> childChunks = c.getChildChunks();

                    DungeonRoom room = new DungeonRoom(c);
                    room.addRoomChunk(c);
                    childChunks.forEach(room::addRoomChunk);
                    room.reevaluateRoomType();
                    layout.addRoom(room);
                }
                RuntimeContext.getInstance().setCurrentDungeonLayout(layout);
                layout.getRooms().forEach(x -> x.setRoomType(x.getRoomType()));
                layout.getRooms().forEach(r -> {
                    r.normalizedRoomType = r.roomType.normalize();
                    r.updateCenterPosition();
                });
                identifyRooms(w);

                return layout;
            } catch (DungeonSecretFinderException e) {
                LOG.log(Level.SEVERE, "An mapped error occurred while scanning the dungeon", e);
                if (e.getClientMessage() != null) {
                    Communication.chatMessage(e.getClientMessage());
                }
                return null;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "An unmapped error occurred while scanning the dungeon", e);
                Communication.chatMessage("§cAn error occurred. Please contact the mod developer with the following error message and your log file!");
                Communication.chatMessage("§e" + e.toString());
                return null;
            }
        }, EXECUTOR_SERVICE);
    }

    private static void identifyRooms(World world) {
        EnumMap<RoomType, RoomIdentificationResolver> resolvers = new EnumMap<>(RoomType.class);
        DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        List<RoomType> distinctRoomTypes = layout.getRooms().stream()
            .map(x -> x.normalizedRoomType)
            .distinct()
            .collect(Collectors.toList());
        distinctRoomTypes.forEach(x -> {
            if (x != null) {
                resolvers.put(x, new RoomIdentificationResolver(x));
            }
        });

        layout.getRooms().forEach(room -> {
            RoomIdentificationResolver resolver = resolvers.get(room.getNormalizedRoomType());
            if (resolver == null) {
                throw new DungeonScannerException("Missing resolver for room type " + room.getNormalizedRoomType());
            }
            for (RoomIdentification identifier : resolver.getIdentifiers()) {
                RoomRotation rotation = RoomRotation.NORTH; // 0 rotations
                boolean allMarkersMatch;
                do {
                    allMarkersMatch = checkForIdentifier(world, room, identifier, rotation);
                } while ((rotation = rotation.rotate()) != RoomRotation.NORTH /* Break after 4 rotations */ && !allMarkersMatch);

                if (allMarkersMatch) {
                    break;
                }
            }
        });

        resolvers.values().forEach(RoomIdentificationResolver::close);
        resolvers.clear();

        List<DungeonRoom> identifiedRooms = layout.getRooms().stream().filter(x -> x.getIdentification() != null).collect(Collectors.toList());
        for (DungeonRoom room : identifiedRooms) {
            try {
                RoomSecretsResolver resolver = new RoomSecretsResolver(room);
                room.setSecrets(new ArrayList<>(resolver.getSecrets()));
                resolver.close();
            } catch (Exception ignored) { }
        }
    }

    private static boolean checkForIdentifier(World world, DungeonRoom room, RoomIdentification identifier, RoomRotation rotation) {
        boolean markersAllMatch = identifier.getMarkers().stream().allMatch(marker -> {
            RelativeLocation rotatedLoc = marker.getLocation();
            for (int i = 0; i < rotation.getRotationAmount(); i++) {
                rotatedLoc = rotatedLoc.rotate();
            }
            BlockPos relativeBlockPos = room.getCenterBlockPos().add(rotatedLoc.getX(), rotatedLoc.getY(), rotatedLoc.getZ());
            Block blockAtRelativeLoc = world.getBlockState(relativeBlockPos).getBlock();
            return blockAtRelativeLoc == marker.getBlock();
        });
        if (markersAllMatch) {
            room.setIdentification(identifier);
            room.setRotation(rotation);
            return true;
        }
        return false;
    }

    private static void connectChunks(DungeonLayout layout, RoomChunk chunk, Predicate<RoomChunk> isInBoundariesCheck) {
        RoomChunk root = chunk;
        while (root.getRootChunk() != null) {
            root = root.getRootChunk();
        }
        RoomChunk finalRoot = root;
        layout.getChunks().stream().filter(isInBoundariesCheck).findFirst().ifPresent(c -> {
            if (c.getRootChunk() == null) {
                c.setRootChunk(finalRoot);
                finalRoot.getChildChunks().add(c);
            } else {
                RoomChunk connectionRoot = c;
                while (connectionRoot.getRootChunk() != null) {
                    connectionRoot = c.getRootChunk();
                }
                if (chunk.getRoomId().equals(finalRoot.getRoomId())) { // finalRoot == chunk -> chunk has no root
                    chunk.setRootChunk(connectionRoot);
                    connectionRoot.getChildChunks().add(chunk);
                }
            }
        });
    }

    private static int findHighestYAt(World w, int x, int z) {
        int y = 255;
        IBlockState bs = w.getBlockState(new BlockPos(x, y, z));
        while (bs.getBlock().getMaterial() == Material.air && y > 0) {
            bs = w.getBlockState(new BlockPos(x, --y, z));
        }
        return y;
    }

}
