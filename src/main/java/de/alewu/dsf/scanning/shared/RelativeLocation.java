package de.alewu.dsf.scanning.shared;

import de.alewu.dsf.exceptions.DungeonSecretFinderException;
import de.alewu.dsf.scanning.DungeonRoom;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.BlockPos;

public class RelativeLocation {

    private final double x;
    private final double y;
    private final double z;

    public RelativeLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RelativeLocation(BlockPos center, BlockPos pos) {
        this(pos.getX() - center.getX(), pos.getY() - center.getY(), pos.getZ() - center.getZ());
    }

    public RelativeLocation(String relativeLocation) {
        String[] args = relativeLocation.split(";");
        if (args.length != 3) {
            throw new DungeonSecretFinderException("args should contain exactly three coordinates but it contained "
                + args.length + " (Array: " + Arrays.toString(args) + ")");
        }
        this.x = Double.parseDouble(args[0]);
        this.y = Double.parseDouble(args[1]);
        this.z = Double.parseDouble(args[2]);
    }

    public RelativeLocation setX(double x) {
        return new RelativeLocation(x, y, z);
    }

    public RelativeLocation setY(double y) {
        return new RelativeLocation(x, y, z);
    }

    public RelativeLocation setZ(double z) {
        return new RelativeLocation(x, y, z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public RelativeLocation rotate() {
        RelativeLocation result = this;
        final double x = result.getX();
        final double z = result.getZ();
        return this.setX(-z).setZ(x);
    }

    public RelativeLocation rotate(RoomRotation rotation) {
        if (rotation == null) {
            return this;
        }
        if (rotation.getRotationAmount() == 0) {
            return this;
        }
        RelativeLocation loc = this;
        for (int i = 0; i < rotation.getRotationAmount(); i++) {
            loc = loc.rotate();
        }
        return loc;
    }

    public boolean equals(BlockPos center, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        float diffX = pos.getX() - center.getX();
        float diffY = pos.getY() - center.getY();
        float diffZ = pos.getZ() - center.getZ();
        return (int) x == diffX && (int) y == diffY && (int) z == diffZ;
    }

    public BlockPos toRealPosition(DungeonRoom room) {
        return room.getCenterBlockPos().add(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelativeLocation that = (RelativeLocation) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
