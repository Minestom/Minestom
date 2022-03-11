package net.minestom.server.map;

import net.minestom.server.MinecraftServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum MapColors {
    NONE(0, 0, 0),
    GRASS(127, 178, 56),
    SAND(247, 233, 163),
    WOOL(199, 199, 199),
    FIRE(255, 0, 0),
    ICE(160, 160, 255),
    METAL(167, 167, 167),
    PLANT(0, 124, 0),
    SNOW(255, 255, 255),
    CLAY(164, 168, 184),
    DIRT(151, 109, 77),
    STONE(112, 112, 112),
    WATER(64, 64, 255),
    WOOD(143, 119, 72),
    QUARTZ(255, 252, 245),
    COLOR_ORANGE(216, 127, 51),
    COLOR_MAGENTA(178, 76, 216),
    COLOR_LIGHT_BLUE(102, 153, 216),
    COLOR_YELLOW(229, 229, 51),
    COLOR_LIGHT_GREEN(127, 204, 25),
    COLOR_PINK(242, 127, 165),
    COLOR_GRAY(76, 76, 76),
    COLOR_LIGHT_GRAY(153, 153, 153),
    COLOR_CYAN(76, 127, 153),
    COLOR_PURPLE(127, 63, 178),
    COLOR_BLUE(51, 76, 178),
    COLOR_BROWN(102, 76, 51),
    COLOR_GREEN(102, 127, 51),
    COLOR_RED(153, 51, 51),
    COLOR_BLACK(25, 25, 25),
    GOLD(250, 238, 77),
    DIAMOND(92, 219, 213),
    LAPIS(74, 128, 255),
    EMERALD(0, 217, 58),
    PODZOL(129, 86, 49),
    NETHER(112, 2, 0),
    TERRACOTTA_WHITE(209, 177, 161),
    TERRACOTTA_ORANGE(159, 82, 36),
    TERRACOTTA_MAGENTA(149, 87, 108),
    TERRACOTTA_LIGHT_BLUE(112, 108, 138),
    TERRACOTTA_YELLOW(186, 133, 36),
    TERRACOTTA_LIGHT_GREEN(103, 117, 53),
    TERRACOTTA_PINK(160, 77, 78),
    TERRACOTTA_GRAY(57, 41, 35),
    TERRACOTTA_LIGHT_GRAY(135, 107, 98),
    TERRACOTTA_CYAN(87, 92, 92),
    TERRACOTTA_PURPLE(122, 73, 88),
    TERRACOTTA_BLUE(76, 62, 92),
    TERRACOTTA_BROWN(76, 50, 35),
    TERRACOTTA_GREEN(76, 82, 42),
    TERRACOTTA_RED(142, 60, 46),
    TERRACOTTA_BLACK(37, 22, 16),
    CRIMSON_NYLIUM(189, 48, 49),
    CRIMSON_STEM(148, 63, 97),
    CRIMSON_HYPHAE(92, 25, 29),
    WARPED_NYLIUM(22, 126, 134),
    WARPED_STEM(58, 142, 140),
    WARPED_HYPHAE(86, 44, 62),
    WARPED_WART_BLOCK(20, 180, 133);

    private final int red;
    private final int green;
    private final int blue;

    private static final ConcurrentHashMap<Integer, PreciseMapColor> rgbMap = new ConcurrentHashMap<>();
    // only used if mappingStrategy == ColorMappingStrategy.PRECISE
    private static volatile PreciseMapColor[] rgbArray = null;

    private static final ColorMappingStrategy mappingStrategy;
    private static final String MAPPING_ARGUMENT = "minestom.map.rgbmapping";
    // only used if MAPPING_ARGUMENT is "approximate"
    private static final String REDUCTION_ARGUMENT = "minestom.map.rgbreduction";
    private static final int colorReduction;

    static {
        ColorMappingStrategy strategy;
        String strategyStr = System.getProperty(MAPPING_ARGUMENT);
        if (strategyStr == null) {
            strategy = ColorMappingStrategy.LAZY;
        } else {
            try {
                strategy = ColorMappingStrategy.valueOf(strategyStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown color mapping strategy: " + strategyStr);
                System.err.println("Defaulting to LAZY.");
                strategy = ColorMappingStrategy.LAZY;
            }
        }
        mappingStrategy = strategy;

        int reduction = 10;
        String reductionStr = System.getProperty(REDUCTION_ARGUMENT);
        if (reductionStr != null) {
            try {
                reduction = Integer.parseInt(reductionStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer in reduction argument: " + reductionStr);
                MinecraftServer.getExceptionManager().handleException(e);
            }

            if (reduction < 0 || reduction >= 255) {
                System.err.println("Reduction was found to be invalid: " + reduction + ". Must in 0-255, defaulting to 10.");
                reduction = 10;
            }
        }
        colorReduction = reduction;
    }

    MapColors(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    // From the wiki: https://minecraft.gamepedia.com/Map_item_format
    // Map Color ID 	Multiply R,G,B By 	= Multiplier
    //Base Color ID*4 + 0 	180 	0.71
    //Base Color ID*4 + 1 	220 	0.86
    //Base Color ID*4 + 2 	255 (same color) 	1
    //Base Color ID*4 + 3 	135 	0.53

    /**
     * Returns the color index with RGB multiplied by 0.53, to use on a map
     */
    public byte multiply53() {
        return (byte) ((ordinal() << 2) + 3);
    }

    /**
     * Returns the color index with RGB multiplied by 0.86, to use on a map
     */
    public byte multiply86() {
        return (byte) ((ordinal() << 2) + 1);
    }

    /**
     * Returns the color index with RGB multiplied by 0.71, to use on a map
     */
    public byte multiply71() {
        return (byte) (ordinal() << 2);
    }

    /**
     * Returns the color index to use on a map
     */
    public byte baseColor() {
        return (byte) ((ordinal() << 2) + 2);
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }

    private static void fillRGBMap() {
        for (MapColors base : values()) {
            if (base == NONE)
                continue;
            for (Multiplier m : Multiplier.values()) {
                PreciseMapColor preciseMapColor = new PreciseMapColor(base, m);
                int rgb = preciseMapColor.toRGB();

                if (mappingStrategy == ColorMappingStrategy.APPROXIMATE) {
                    rgb = reduceColor(rgb);
                }
                rgbMap.put(rgb, preciseMapColor);
            }
        }
    }

    private static void fillRGBArray() {
        rgbArray = new PreciseMapColor[0xFFFFFF + 1];
        for (int rgb = 0; rgb <= 0xFFFFFF; rgb++) {
            rgbArray[rgb] = mapColor(rgb);
        }
    }

    public static PreciseMapColor closestColor(int argb) {
        int noAlpha = argb & 0xFFFFFF;
        if (mappingStrategy == ColorMappingStrategy.PRECISE) {
            if (rgbArray == null) {
                synchronized (MapColors.class) {
                    if (rgbArray == null) {
                        fillRGBArray();
                    }
                }
            }
            return rgbArray[noAlpha];
        }
        if (rgbMap.isEmpty()) {
            synchronized (rgbMap) {
                if (rgbMap.isEmpty()) {
                    fillRGBMap();
                }
            }
        }
        if (mappingStrategy == ColorMappingStrategy.APPROXIMATE) {
            noAlpha = reduceColor(noAlpha);
        }
        return rgbMap.computeIfAbsent(noAlpha, MapColors::mapColor);
    }

    private static int reduceColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        red = red / colorReduction;
        green = green / colorReduction;
        blue = blue / colorReduction;
        return (red << 16) | (green << 8) | blue;
    }

    private static PreciseMapColor mapColor(int rgb) {
        PreciseMapColor closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (MapColors base : values()) {
            if (base == NONE)
                continue;
            for (Multiplier m : Multiplier.values()) {
                final int rgbKey = PreciseMapColor.toRGB(base, m);
                final int redKey = (rgbKey >> 16) & 0xFF;
                final int greenKey = (rgbKey >> 8) & 0xFF;
                final int blueKey = rgbKey & 0xFF;

                final int red = (rgb >> 16) & 0xFF;
                final int green = (rgb >> 8) & 0xFF;
                final int blue = rgb & 0xFF;

                final int dr = redKey - red;
                final int dg = greenKey - green;
                final int db = blueKey - blue;
                final int dist = (dr * dr + dg * dg + db * db);
                if (dist < closestDistance) {
                    closest = new PreciseMapColor(base, m);
                    closestDistance = dist;
                }
            }
        }
        return closest;
    }

    public static class PreciseMapColor {
        private final MapColors baseColor;
        private final Multiplier multiplier;

        PreciseMapColor(MapColors base, Multiplier multiplier) {
            this.baseColor = base;
            this.multiplier = multiplier;
        }

        public MapColors getBaseColor() {
            return baseColor;
        }

        public Multiplier getMultiplier() {
            return multiplier;
        }

        public byte getIndex() {
            return multiplier.apply(baseColor);
        }

        public int toRGB() {
            return toRGB(baseColor, multiplier);
        }

        public static int toRGB(MapColors baseColor, Multiplier multiplier) {
            double r = baseColor.red();
            double g = baseColor.green();
            double b = baseColor.blue();

            r *= multiplier.multiplier();
            g *= multiplier.multiplier();
            b *= multiplier.multiplier();

            final int red = (int) r;
            final int green = (int) g;
            final int blue = (int) b;
            return (red << 16) | (green << 8) | blue;
        }
    }

    public enum Multiplier {
        x1_00(MapColors::baseColor, 1.00),
        x0_53(MapColors::multiply53, 0.53),
        x0_71(MapColors::multiply71, 0.71),
        x0_86(MapColors::multiply86, 0.86);

        private final Function<MapColors, Byte> indexGetter;
        private final double multiplier;

        Multiplier(Function<MapColors, Byte> indexGetter, double multiplier) {
            this.indexGetter = indexGetter;
            this.multiplier = multiplier;
        }

        public double multiplier() {
            return multiplier;
        }

        public byte apply(MapColors baseColor) {
            return indexGetter.apply(baseColor);
        }
    }

    /**
     * How does Minestom compute RGB to MapColor transitions?
     */
    public enum ColorMappingStrategy {
        /**
         * If already computed, send the result. Otherwise, compute the closest color in a RGB Map, and add it to the map
         */
        LAZY,

        /**
         * All colors are already in the map after the first call. Heavy hit on the memory:
         * (2^24) * 4 bytes at the min (~64MB)
         */
        PRECISE,

        /**
         * RGB components are divided by 10 before issuing a lookup (as with the PRECISE strategy), but saves on memory usage
         */
        APPROXIMATE
    }
}
