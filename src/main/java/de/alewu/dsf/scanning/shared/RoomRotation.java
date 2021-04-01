package de.alewu.dsf.scanning.shared;

public enum RoomRotation {

    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(3);

    private final int rotationAmount;

    RoomRotation(int rotationAmount) {
        this.rotationAmount = rotationAmount;
    }

    public RoomRotation rotate() {
        switch (this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
        }
        throw new IllegalArgumentException("Unmapped rotation: " + this);
    }

    public int getRotationAmount() {
        return rotationAmount;
    }
}
