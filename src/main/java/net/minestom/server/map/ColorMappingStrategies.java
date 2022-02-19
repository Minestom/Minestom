package net.minestom.server.map;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

enum ColorMappingStrategies implements ColorMappingStrategy {
    /**
     * All colors are already in the map after the first call. Heavy hit on the memory:
     * (2^24) * 4 bytes at the min (~64MB)
     */
    PRECISE {
        private static volatile PreciseMapColor[] rgbArray = null;

        private static void fillRGBArray() {
            rgbArray = new PreciseMapColor[0xFFFFFF + 1];
            for (int rgb = 0; rgb <= 0xFFFFFF; rgb++) {
                rgbArray[rgb] = mapColor(rgb);
            }
        }

        @Override
        public void populateCache() {
            if (rgbArray == null) {
                synchronized (ColorMappingStrategies.class) {
                    if (rgbArray == null) {
                        fillRGBArray();
                    }
                }
            }
        }

        @Override
        public PreciseMapColor retrieveClosestColor(int noAlpha) {
            return rgbArray[noAlpha];
        }
    },

    /**
     * If already computed, send the result. Otherwise, compute the closest color in a RGB Map, and add it to the map
     */
    LAZY {
        private static final ConcurrentHashMap<Integer, PreciseMapColor> rgbMap = new ConcurrentHashMap<>();

        private static void fillRGBMap() {
            for (MapColor base : MapColor.values()) {
                if (base == MapColor.NONE)
                    continue;
                for (MapColorMultiplier m : MapColorMultiplier.values()) {
                    PreciseMapColor preciseMapColor = new PreciseMapColor(base, m);
                    int rgb = preciseMapColor.toRGB();

                    rgbMap.put(rgb, preciseMapColor);
                }
            }
        }

        @Override
        public void populateCache() {
            if (rgbMap.isEmpty()) {
                synchronized (rgbMap) {
                    if (rgbMap.isEmpty()) {
                        fillRGBMap();
                    }
                }
            }
        }

        @Override
        public PreciseMapColor retrieveClosestColor(int noAlpha) {
            return rgbMap.computeIfAbsent(noAlpha, ColorMappingStrategies::mapColor);
        }
    },

    /**
     * RGB components are divided by 10 before issuing a lookup (as with the PRECISE strategy), but saves on memory usage
     */
    APPROXIMATE {
        private static final String DEFAULT_REDUCTION_ARGUMENT = "minestom.map.rgbreduction";
        private static final int colorReduction;
        private static final ConcurrentHashMap<Integer, PreciseMapColor> rgbMap = new ConcurrentHashMap<>();

        static {
            int reduction = 10;
            String reductionStr = System.getProperty(DEFAULT_REDUCTION_ARGUMENT);
            if (reductionStr != null) {
                try {
                    reduction = Integer.parseInt(reductionStr);
                } catch (NumberFormatException e) {
                    LOGGER.error("Invalid integer in reduction argument: " + reductionStr);
                    MinecraftServer.getExceptionManager().handleException(e);
                }

                if (reduction < 0 || reduction >= 255) {
                    LOGGER.error("Reduction was found to be invalid: " + reduction + ". Must in 0-255, defaulting to 10.");
                    reduction = 10;
                }
            }
            colorReduction = reduction;
        }

        private static void fillRGBMap() {
            for (MapColor base : MapColor.values()) {
                if (base == MapColor.NONE)
                    continue;
                for (MapColorMultiplier m : MapColorMultiplier.values()) {
                    PreciseMapColor preciseMapColor = new PreciseMapColor(base, m);
                    int rgb = reduceColor(preciseMapColor.toRGB());

                    rgbMap.put(rgb, preciseMapColor);
                }
            }
        }

        @Override
        public void populateCache() {
            if (rgbMap.isEmpty()) {
                synchronized (rgbMap) {
                    if (rgbMap.isEmpty()) {
                        fillRGBMap();
                    }
                }
            }
        }

        @Override
        public PreciseMapColor retrieveClosestColor(int noAlpha) {
            noAlpha = reduceColor(noAlpha);
            return rgbMap.computeIfAbsent(noAlpha, ColorMappingStrategies::mapColor);
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

    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorMappingStrategies.class);

    @NotNull
    private static PreciseMapColor mapColor(int rgb) {
        PreciseMapColor closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (MapColor base : MapColor.values()) {
            if (base == MapColor.NONE)
                continue;
            for (MapColorMultiplier m : MapColorMultiplier.values()) {
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
        // Return closest or the empty MapColor if this is null (shouldn't be possible!)
        return closest == null ? new PreciseMapColor(MapColor.NONE, MapColorMultiplier.x1_00) : closest;
    }

    public abstract PreciseMapColor retrieveClosestColor(int argb);

    public abstract void populateCache();

    @Override
    @NotNull
    public final PreciseMapColor closestColor(int argb) {
        populateCache();
        // argb & 0xFFFFFF removes the alpha from the argb, leaving us with rgb.
        return retrieveClosestColor(argb & 0xFFFFFF);
    }
}
