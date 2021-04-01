package de.alewu.dsf.util;

import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

public class ColorMap {

    public static final int BLACK = 0;
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int CYAN = 3;
    public static final int RED = 4;
    public static final int MAGENTA = 5;
    public static final int YELLOW = 6;
    public static final int WHITE = 7;
    private static final Map<Integer, ColorData> COLOR_MAP = ImmutableMap.<Integer, ColorData>builder()
        .put(0, new ColorData("Black", new Color(0, 0, 0)))
        .put(1, new ColorData("Blue", new Color(0, 0, 255)))
        .put(2, new ColorData("Green", new Color(0, 255, 0)))
        .put(3, new ColorData("Cyan", new Color(0, 255, 255)))
        .put(4, new ColorData("Red", new Color(255, 0, 0)))
        .put(5, new ColorData("Magenta", new Color(255, 0, 255)))
        .put(6, new ColorData("Yellow", new Color(255, 255, 0)))
        .put(7, new ColorData("White", new Color(255, 255, 255)))
        .build();

    public static ColorData getColorData(int id) {
        return COLOR_MAP.getOrDefault(id, new ColorData("Undefined", new Color(0, 0, 0)));
    }

    public static ColorData getColorData(String name) {
        return COLOR_MAP.entrySet().stream().filter(x -> x.getValue().name.equalsIgnoreCase(name))
            .findFirst()
            .orElse(new SimpleEntry<>(-1, new ColorData("Undefined", new Color(0, 0, 0))))
            .getValue();
    }

    public static Map<Integer, ColorData> getColorMap() {
        return COLOR_MAP;
    }

    public static int getColormapId(ColorData data) {
        for (Entry<Integer, ColorData> entry : COLOR_MAP.entrySet()) {
            if (entry.getValue().name.equals(data.name)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static class ColorData {

        private final String name;
        private final Color color;

        public ColorData(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }
}
