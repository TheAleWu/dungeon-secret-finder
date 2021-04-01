package de.alewu.dsf.scanning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public enum RoomType {

    /**
     * <b>Shape:</b>
     * <br>A
     * <br><i>A = Anchor point</i>
     */
    ONE_BY_ONE(null, "1x1.json", 1, 1, deltaToCenter(1, 1), Arrays.asList(Pair.of(0, 0))),
    /**
     * <b>Shape:</b>
     * <br>A X
     * <br><i>A = Anchor point</i>
     */
    TWO_BY_ONE(null, "2x1.json", 2, 1, deltaToCenter(2, 1), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0))),
    /**
     * <b>Shape:</b>
     * <br>A X X
     * <br><i>A = Anchor point</i>
     */
    THREE_BY_ONE(null, "3x1.json", 3, 1, deltaToCenter(3, 1), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0))),
    /**
     * <b>Shape:</b>
     * <br>A X X X
     * <br><i>A = Anchor point</i>
     */
    FOUR_BY_ONE(null, "4x1.json", 4, 1, deltaToCenter(4, 1), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0), Pair.of(3, 0))),
    /**
     * <b>Shape:</b>
     * <br>A
     * <br>X
     * <br><i>A = Anchor point</i>
     */
    ONE_BY_TWO(TWO_BY_ONE, "2x1.json", 1, 2, deltaToCenter(1, 2), Arrays.asList(Pair.of(0, 0), Pair.of(0, 1))),
    /**
     * <b>Shape:</b>
     * <br>A
     * <br>X
     * <br>X
     * <br><i>A = Anchor point</i>
     */
    ONE_BY_THREE(THREE_BY_ONE, "3x1.json", 1, 3, deltaToCenter(1, 3), Arrays.asList(Pair.of(0, 0), Pair.of(0, 1), Pair.of(0, 2))),
    /**
     * <b>Shape:</b>
     * <br>A
     * <br>X
     * <br>X
     * <br>X
     * <br><i>A = Anchor point</i>
     */
    ONE_BY_FOUR(FOUR_BY_ONE, "4x1.json", 1, 4, deltaToCenter(1, 4), Arrays.asList(Pair.of(0, 0), Pair.of(0, 1), Pair.of(0, 2), Pair.of(0, 3))),
    /**
     * <b>Shape:</b>
     * <br>A X <b>|</b> X X
     * <br>X X <b>|</b> X X
     * <br><i>A = Anchor point</i>
     */
    TWO_BY_TWO(null, "2x2.json", 2, 2, deltaToCenter(2, 2), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0), Pair.of(0, 1), Pair.of(1, 1))),
    /**
     * <b>Shape:</b>
     * <br>A +
     * <br>X X
     * <br><i>A = Anchor point | + = "Empty"/Ignored space</i>
     */
    L_SHAPED(null, "l.json", 2, 2, deltaToCenter(2, 2), Arrays.asList(Pair.of(0, 0), Pair.of(0, 1), Pair.of(1, 1))),
    /**
     * <b>Shape:</b>
     * <br>A X <b>|</b> + X
     * <br>X X <b>|</b> X X
     * <br><i>A = Anchor point</i>
     */
    L_MIRRORED(L_SHAPED, "l.json", 2, 2, deltaToCenter(2, 2), Arrays.asList(Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 1))),
    /**
     * <b>Shape:</b>
     * <br>A X
     * <br>X +
     * <br><i>A = Anchor point | + = "Empty"/Ignored space</i>
     */
    L_FLIPPED(L_SHAPED, "l.json", 2, 2, deltaToCenter(2, 2), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0), Pair.of(0, 1))),
    /**
     * <b>Shape:</b>
     * <br>A X
     * <br>+ X
     * <br><i>A = Anchor point | + = "Empty"/Ignored space</i>
     */
    L_FLIPPED_AND_MIRRORED(L_SHAPED, "l.json", 2, 2, deltaToCenter(2, 2), Arrays.asList(Pair.of(0, 0), Pair.of(1, 0), Pair.of(1, 1))),
    /**
     * <b>Shape:</b>
     * <br>?
     */
    UNRECOGNIZABLE(null, null, 0, 0, deltaToCenter(0, 0), new ArrayList<>());

    private final RoomType normalized;
    private final String markersFileName;
    private final int multiplierX;
    private final int multiplierZ;
    private final Pair<Integer, Integer> deltaToCenter;
    private final List<Pair<Integer, Integer>> checkedPositions;

    RoomType(RoomType normalized, String markersFileName, int multiplierX, int multiplierZ,
        Pair<Integer, Integer> deltaToCenter, List<Pair<Integer, Integer>> checkedPositions) {
        this.normalized = normalized;
        this.markersFileName = markersFileName;
        this.multiplierX = multiplierX;
        this.multiplierZ = multiplierZ;
        this.deltaToCenter = deltaToCenter;
        this.checkedPositions = checkedPositions;
    }

    public RoomType getNormalized() {
        return normalized;
    }

    public String getMarkersFileName() {
        return markersFileName;
    }

    public int getMultiplierX() {
        return multiplierX;
    }

    public int getMultiplierZ() {
        return multiplierZ;
    }

    public int getPriority() {
        return checkedPositions.size();
    }

    public Pair<Integer, Integer> getDeltaToCenter() {
        return deltaToCenter;
    }

    public List<Pair<Integer, Integer>> getCheckedPositions() {
        return checkedPositions;
    }

    public RoomType normalize() {
        return getNormalized() != null ? getNormalized() : this;
    }

    private static Pair<Integer, Integer> deltaToCenter(int scalingX, int scalingZ) {
        return Pair.of((RoomChunk.CHUNK_SIZE_X * scalingX - 2) / 2, (RoomChunk.CHUNK_SIZE_Z * scalingZ - 2) / 2);
    }
}
