package net.minestom.server.map;

import java.util.function.Function;

public enum MapColors {
    NONE(0,0,0),
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
    WARPED_WART_BLOCK(20, 180, 133),
    ;

    private final int red;
    private final int green;
    private final int blue;

    MapColors(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    // From the wiki: https://minecraft.gamepedia.com/Map_item_format
    // Map Color ID 	Multiply R,G,B By 	= Multiplier
    //Base Color ID×4 + 0 	180 	0.71
    //Base Color ID×4 + 1 	220 	0.86
    //Base Color ID×4 + 2 	255 (same color) 	1
    //Base Color ID×4 + 3 	135 	0.53

    /**
     * Returns the color index with RGB multiplied by 0.53, to use on a map
     */
    public byte multiply53() {
        return (byte) ((ordinal() << 2) +3);
    }

    /**
     * Returns the color index with RGB multiplied by 0.86, to use on a map
     */
    public byte multiply86() {
        return (byte) ((ordinal() << 2) +1);
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
        return (byte) ((ordinal() << 2) +2);
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

    public PreciseMapColor closestColor(int rgb) {
        throw new UnsupportedOperationException("TODO");
    }

    public static class PreciseMapColor {
        private MapColors baseColor;
        private Multiplier multiplier;

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
    }

    enum Multiplier {
        x1_00(MapColors::baseColor),
        x0_53(MapColors::multiply53),
        x0_71(MapColors::multiply71),
        x0_86(MapColors::multiply86),
        ;

        private final Function<MapColors, Byte> indexGetter;

        Multiplier(Function<MapColors, Byte> indexGetter) {
            this.indexGetter = indexGetter;
        }

        public byte apply(MapColors baseColor) {
            return indexGetter.apply(baseColor);
        }
    }
}
