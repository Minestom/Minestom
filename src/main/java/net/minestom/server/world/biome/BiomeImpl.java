package net.minestom.server.world.biome;

import net.minestom.server.color.Color;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record BiomeImpl(
        float temperature,
        float downfall,
        @NotNull BiomeEffects effects,
        boolean hasPrecipitation,
        @NotNull TemperatureModifier temperatureModifier,
        @Nullable Registry.BiomeEntry registry
) implements Biome {
    // https://minecraft.wiki/w/Rain
    private final static double SNOW_TEMPERATURE = 0.15;

    BiomeImpl(Registry.BiomeEntry entry) {
        this(entry.temperature(), entry.downfall(), getBuilder(entry).build(),
                entry.hasPrecipitation(),
                entry.temperature() < SNOW_TEMPERATURE ? TemperatureModifier.FROZEN : TemperatureModifier.NONE,
                entry
        );
    }

    @NotNull
    private static BiomeEffects.Builder getBuilder(Registry.BiomeEntry entry) {
        BiomeEffects.Builder effectsBuilder = BiomeEffects.builder();
        if (entry.foliageColor() != null) effectsBuilder.foliageColor(new Color(entry.foliageColor()));
        if (entry.grassColor() != null) effectsBuilder.grassColor(new Color(entry.grassColor()));
        if (entry.skyColor() != null) effectsBuilder.skyColor(new Color(entry.skyColor()));
        if (entry.waterColor() != null) effectsBuilder.waterColor(new Color(entry.waterColor()));
        if (entry.waterFogColor() != null) effectsBuilder.waterFogColor(new Color(entry.waterFogColor()));
        if (entry.fogColor() != null) effectsBuilder.fogColor(new Color(entry.fogColor()));
        return effectsBuilder;
    }

}
