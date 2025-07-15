package net.minestom.server.world.biome;


record BiomeImpl(
        float temperature,
        float downfall,
        BiomeEffects effects,
        boolean hasPrecipitation,
        TemperatureModifier temperatureModifier
) implements Biome {
    // https://minecraft.wiki/w/Rain
    private final static double SNOW_TEMPERATURE = 0.15;

}
