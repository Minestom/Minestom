package net.minestom.server.world.biome;

import org.jetbrains.annotations.NotNull;

record BiomeImpl(
        float temperature,
        float downfall,
        @NotNull BiomeEffects effects,
        boolean hasPrecipitation,
        @NotNull TemperatureModifier temperatureModifier
) implements Biome {
    // https://minecraft.wiki/w/Rain
    private final static double SNOW_TEMPERATURE = 0.15;

}
