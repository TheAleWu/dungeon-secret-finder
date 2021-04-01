package de.alewu.dsf.scanning.identification;

import de.alewu.dsf.scanning.shared.RelativeLocation;
import net.minecraft.block.Block;

public class RoomIdentificationMarker {

    private final Block block;
    private final RelativeLocation location;

    public RoomIdentificationMarker(Block block, RelativeLocation location) {
        this.block = block;
        this.location = location;
    }

    public Block getBlock() {
        return block;
    }

    public RelativeLocation getLocation() {
        return location;
    }
}
