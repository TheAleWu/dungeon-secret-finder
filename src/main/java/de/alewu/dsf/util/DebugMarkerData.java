package de.alewu.dsf.util;

import de.alewu.dsf.util.ColorMap.ColorData;
import net.minecraft.util.AxisAlignedBB;
import org.apache.commons.lang3.tuple.Triple;

public class DebugMarkerData {

    private final boolean onCursor;
    private final ColorData markerColor;
    private final AxisAlignedBB boundingBox;
    private final Triple<Float, Float, Float> translation;

    public DebugMarkerData(boolean onCursor, ColorData markerColor, AxisAlignedBB boundingBox, Triple<Float, Float, Float> translation) {
        this.onCursor = onCursor;
        this.markerColor = markerColor;
        this.boundingBox = boundingBox;
        this.translation = translation;
    }

    public boolean isOnCursor() {
        return onCursor;
    }

    public ColorData getMarkerColor() {
        return markerColor;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public Triple<Float, Float, Float> getTranslation() {
        return translation;
    }
}
