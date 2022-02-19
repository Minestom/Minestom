package net.minestom.server.map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class PreciseMapColor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreciseMapColor.class);

    private static final ColorMappingStrategy globalColorMappingStrategy;
    private static final String MAPPING_ARGUMENT = "minestom.map.rgbmapping";

    static {
        String strategyStr = System.getProperty(MAPPING_ARGUMENT);
        if (strategyStr == null) {
            globalColorMappingStrategy = ColorMappingStrategies.LAZY;
        } else {
            switch (strategyStr.trim().toLowerCase(Locale.ROOT)) {
                case "precise" -> globalColorMappingStrategy = ColorMappingStrategies.PRECISE;
                case "approximate" -> globalColorMappingStrategy = ColorMappingStrategies.APPROXIMATE;
                case "lazy" -> globalColorMappingStrategy = ColorMappingStrategies.LAZY;
                default -> {
                    LOGGER.error("Unknown color mapping strategy: " + strategyStr);
                    LOGGER.error("Defaulting to LAZY.");
                    globalColorMappingStrategy = ColorMappingStrategies.LAZY;
                }
            }
        }
    }

    private final MapColor baseColor;
    private final MapColorMultiplier multiplier;

    public PreciseMapColor(@NotNull MapColor base, @NotNull MapColorMultiplier multiplier) {
        this.baseColor = base;
        this.multiplier = multiplier;
    }

    @NotNull
    public static PreciseMapColor closestColor(int argb) {
        return closestColor(argb, globalColorMappingStrategy);
    }

    @NotNull
    public static PreciseMapColor closestColor(int argb, @NotNull ColorMappingStrategy strategy) {
        return strategy.closestColor(argb);
    }

    public static int toRGB(@NotNull MapColor baseColor, @NotNull MapColorMultiplier multiplier) {
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

    @NotNull
    public MapColor getBaseColor() {
        return baseColor;
    }

    @NotNull
    public MapColorMultiplier getMultiplier() {
        return multiplier;
    }

    public byte getIndex() {
        return multiplier.apply(baseColor);
    }

    public int toRGB() {
        return toRGB(baseColor, multiplier);
    }
}