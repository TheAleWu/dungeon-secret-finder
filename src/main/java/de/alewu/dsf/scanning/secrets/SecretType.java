package de.alewu.dsf.scanning.secrets;

import de.alewu.dsf.util.ColorMap;
import de.alewu.dsf.util.ColorMap.ColorData;
import net.minecraft.util.AxisAlignedBB;

public enum SecretType {

    LEVER("Flick Lever", new AxisAlignedBB(0.25, 0, 0.75, 0.75, 0.6, 0.25), ColorMap.getColorData(ColorMap.CYAN)),
    BUTTON("Press button", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.CYAN)),
    CHEST("Open chest", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.GREEN)),
    LOCATION("Reach block", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.BLUE)),
    BAT("Look for bat", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.YELLOW)),
    INTERACT("Interact with block / entity", new AxisAlignedBB(0.1, 0, 0.1, 0.9, 0.6, 0.9), ColorMap.getColorData(ColorMap.BLACK)),
    PLACE("Place item", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.WHITE)),
    COLLECT_ITEM("Pick up item", new AxisAlignedBB(0.2, 0, 0.2, 0.8, 0.5, 0.8), ColorMap.getColorData(ColorMap.MAGENTA)),
    DESTROY("Destroy blocks", getAxisAlignedBB(), ColorMap.getColorData(ColorMap.RED));

    private static AxisAlignedBB getAxisAlignedBB() {
        return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    }

    private final String defaultComment;
    private final AxisAlignedBB defaultBorderSize;
    private final ColorData defaultMarkerColor;

    SecretType(String defaultComment, AxisAlignedBB defaultBorderSize, ColorData defaultMarkerColor) {
        this.defaultComment = defaultComment;
        this.defaultBorderSize = defaultBorderSize;
        this.defaultMarkerColor = defaultMarkerColor;
    }

    public String getDefaultComment() {
        return defaultComment;
    }

    public ColorData getDefaultMarkerColor() {
        return defaultMarkerColor;
    }

    public AxisAlignedBB getDefaultBorderSize() {
        return defaultBorderSize;
    }
}
