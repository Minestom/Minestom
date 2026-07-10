package net.minestom.server.world.biome;

import net.minestom.server.world.attribute.EnvironmentAttributeMap;

import java.util.Objects;

record BiomeImpl(
        boolean hasPrecipitation,
        float temperature,
        TemperatureModifier temperatureModifier,
        float downfall,
        EnvironmentAttributeMap attributes,
        BiomeEffects effects
) implements Biome {

    public BiomeImpl {
        Objects.requireNonNull(temperatureModifier, "temperatureModifier");
        Objects.requireNonNull(attributes, "attributes");
        Objects.requireNonNull(effects, "effects");
    }

}
