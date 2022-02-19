package net.minestom.server.map;

import java.util.function.Function;

public enum MapColorMultiplier {
    x1_00(MapColor::baseColor, 1.00),
    x0_53(MapColor::multiply53, 0.53),
    x0_71(MapColor::multiply71, 0.71),
    x0_86(MapColor::multiply86, 0.86);

    private final Function<MapColor, Byte> indexGetter;
    private final double multiplier;

    MapColorMultiplier(Function<MapColor, Byte> indexGetter, double multiplier) {
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