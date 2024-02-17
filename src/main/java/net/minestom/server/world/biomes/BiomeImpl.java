package net.minestom.server.world.biomes;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

final class BiomeImpl implements ProtocolObject, Biome {
    // https://minecraft.wiki/w/Rain
    private final static Double SNOW_TEMPERATURE = 0.15;

    private static final Registry.DynamicContainer<BiomeImpl> CONTAINER = Registry.createDynamicContainer(Registry.Resource.BIOMES,
            (namespace, properties) -> new BiomeImpl(Registry.biome(namespace, properties)));

    static Collection<BiomeImpl> values() {
        return CONTAINER.values();
    }

    static BiomeImpl get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static BiomeImpl getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    private Registry.BiomeEntry entry;
    @NotNull
    private final NamespaceID name;
    private final float depth;
    private final float temperature;
    private final float scale;
    private final float downfall;
    @NotNull
    private final BiomeEffects effects;
    @NotNull
    private final Precipitation precipitation;
    @NotNull
    private final TemperatureModifier temperatureModifier;

    BiomeImpl(NamespaceID name, float depth, float temperature, float scale, float downfall, BiomeEffects effects, Precipitation precipitation, TemperatureModifier temperatureModifier) {
        this.name = name;
        this.depth = depth;
        this.temperature = temperature;
        this.scale = scale;
        this.downfall = downfall;
        this.effects = effects;
        this.precipitation = precipitation;
        this.temperatureModifier = temperatureModifier;
    }

    BiomeImpl(Registry.BiomeEntry entry) {
        this.entry = entry;
        this.name = entry.namespace();
        this.depth = 0.2f;
        this.scale = 0.2f;
        this.temperature = entry.temperature();

        BiomeEffects.Builder effectsBuilder = getBuilder(entry);
        this.effects = effectsBuilder.build();

        this.precipitation = entry.hasPrecipitation()
                ? temperature < SNOW_TEMPERATURE
                    ? Biome.Precipitation.SNOW
                    : Biome.Precipitation.RAIN
                : Biome.Precipitation.NONE;

        this.downfall = entry.downfall();
        this.temperatureModifier = entry.temperature() < SNOW_TEMPERATURE ? TemperatureModifier.FROZEN : TemperatureModifier.NONE;
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

    @Nullable
    @Override
    public Registry.BiomeEntry registry() {
        return this.entry;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return this.name;
    }

    public float depth() {
        return this.depth;
    }

    public float temperature() {
        return this.temperature;
    }

    public float scale() {
        return this.scale;
    }

    public float downfall() {
        return this.downfall;
    }

    public @NotNull BiomeEffects effects() {
        return this.effects;
    }

    public @NotNull Precipitation precipitation() {
        return this.precipitation;
    }

    public @NotNull TemperatureModifier temperatureModifier() {
        return this.temperatureModifier;
    }
}
