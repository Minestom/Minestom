package net.minestom.server.map;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MapColor implements RGBLike {
    private static final ConcurrentHashMap<Integer, PreciseMapColor> rgbMap = new ConcurrentHashMap<>();
    // only used if mappingStrategy == ColorMappingStrategy.PRECISE
    private static PreciseMapColor[] rgbArray = null;

    private static final ColorMappingStrategy mappingStrategy;
    private static final String MAPPING_ARGUMENT = "minestom.map.rgbmapping";
    // only used if MAPPING_ARGUMENT is "approximate"
    private static final String REDUCTION_ARGUMENT = "minestom.map.rgbreduction";
    private static final int colorReduction;

    private final int id;
    private final int red;
    private final int green;
    private final int blue;


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

    MapColor(int id, int red, int green, int blue) {
        this.id = id;
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
        return (byte) ((id << 2) + 3);
    }

    /**
     * Returns the color index with RGB multiplied by 0.86, to use on a map
     */
    public byte multiply86() {
        return (byte) ((id << 2) + 1);
    }

    /**
     * Returns the color index with RGB multiplied by 0.71, to use on a map
     */
    public byte multiply71() {
        return (byte) (id << 2);
    }

    /**
     * Returns the color index to use on a map
     */
    public byte baseColor() {
        return (byte) ((id << 2) + 2);
    }

    public int getId() {
        return id;
    }

    @Override
    public int red() {
        return red;
    }

    @Override
    public int green() {
        return green;
    }

    @Override
    public int blue() {
        return blue;
    }

    private static void fillRGBMap() {
        for (MapColor base : MapColors.values()) {
            if (base == MapColors.NONE)
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
        MinestomThread threads = new MinestomThread(Runtime.getRuntime().availableProcessors(), "RGBMapping", true);
        for (int rgb = 0; rgb <= 0xFFFFFF; rgb++) {
            int finalRgb = rgb;
            threads.execute(() -> rgbArray[finalRgb] = mapColor(finalRgb));
        }
        try {
            threads.shutdown();
            threads.awaitTermination(100, TimeUnit.MINUTES);
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(t);
        }
    }

    public static PreciseMapColor closestColor(int argb) {
        int noAlpha = argb & 0xFFFFFF;
        if (mappingStrategy == ColorMappingStrategy.PRECISE) {
            if (rgbArray == null) {
                synchronized (MapColor.class) {
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
        return rgbMap.computeIfAbsent(noAlpha, MapColor::mapColor);
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
        for (MapColor base : MapColors.values()) {
            if (base == MapColors.NONE)
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
        private final MapColor baseColor;
        private final Multiplier multiplier;

        PreciseMapColor(MapColor base, Multiplier multiplier) {
            this.baseColor = base;
            this.multiplier = multiplier;
        }

        public MapColor getBaseColor() {
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

        public static int toRGB(MapColor baseColor, Multiplier multiplier) {
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
        x1_00(MapColor::baseColor, 1.00),
        x0_53(MapColor::multiply53, 0.53),
        x0_71(MapColor::multiply71, 0.71),
        x0_86(MapColor::multiply86, 0.86);

        private final Function<MapColor, Byte> indexGetter;
        private final double multiplier;

        Multiplier(Function<MapColor, Byte> indexGetter, double multiplier) {
            this.indexGetter = indexGetter;
            this.multiplier = multiplier;
        }

        public double multiplier() {
            return multiplier;
        }

        public byte apply(MapColor baseColor) {
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
