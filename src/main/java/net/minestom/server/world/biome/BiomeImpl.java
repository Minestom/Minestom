package net.minestom.server.world.biome;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

record BiomeImpl(
        float temperature,
        float downfall,
        @NotNull BiomeEffects effects,
        @NotNull Precipitation precipitation,
        @NotNull TemperatureModifier temperatureModifier,
        @Nullable Registry.BiomeEntry registry
) implements Biome {
    // https://minecraft.wiki/w/Rain
    private final static Double SNOW_TEMPERATURE = 0.15;

    static final BinaryTagSerializer<Biome> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("Biome is read-only");
            },
            biome -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
                        .putFloat("temperature", biome.temperature())
                        .putFloat("downfall", biome.downfall())
                        .putByte("has_precipitation", (byte) (biome.precipitation() == Precipitation.NONE ? 0 : 1))
                        .putString("precipitation", biome.precipitation().name().toLowerCase(Locale.ROOT));
                if (biome.temperatureModifier() != TemperatureModifier.NONE)
                    builder.putString("temperature_modifier", biome.temperatureModifier().name().toLowerCase(Locale.ROOT));
                return builder
                        .put("effects", biome.effects().toNbt())
                        .build();
            }
    );

    BiomeImpl(Registry.BiomeEntry entry) {
        this(entry.temperature(), entry.downfall(), getBuilder(entry).build(),
                entry.hasPrecipitation()
                        ? entry.temperature() < SNOW_TEMPERATURE
                        ? Precipitation.SNOW
                        : Precipitation.RAIN
                        : Precipitation.NONE,
                entry.temperature() < SNOW_TEMPERATURE ? TemperatureModifier.FROZEN : TemperatureModifier.NONE,
                entry
        );
    }

    @NotNull
    private static BiomeEffects.Builder getBuilder(Registry.BiomeEntry entry) {
        BiomeEffects.Builder effectsBuilder = BiomeEffects.builder();
        if (entry.foliageColor() != null) effectsBuilder.foliageColor(entry.foliageColor());
        if (entry.grassColor() != null) effectsBuilder.grassColor(entry.grassColor());
        if (entry.skyColor() != null) effectsBuilder.skyColor(entry.skyColor());
        if (entry.waterColor() != null) effectsBuilder.waterColor(entry.waterColor());
        if (entry.waterFogColor() != null) effectsBuilder.waterFogColor(entry.waterFogColor());
        if (entry.fogColor() != null) effectsBuilder.fogColor(entry.fogColor());
        return effectsBuilder;
    }

}
