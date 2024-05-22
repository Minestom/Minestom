package net.minestom.server.world.biome;

import java.util.Locale;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Biome extends Biomes, ProtocolObject permits BiomeImpl {

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for biomes, loading the vanilla trim biomes.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Biome> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:worldgen/biome", BiomeImpl.REGISTRY_NBT_TYPE, Registry.Resource.BIOMES,
                (namespace, props) -> new BiomeImpl(Registry.biome(namespace, props)),
                // We force plains to be first because it allows convenient palette initialization.
                // Maybe worth switching to fetching plains in the palette in the future to avoid this.
                (a, b) -> a.equals("minecraft:plains") ? -1 : b.equals("minecraft:plains") ? 1 : 0
        );
    }

    float temperature();

    float downfall();

    @NotNull BiomeEffects effects();

    @NotNull Precipitation precipitation();

    @NotNull TemperatureModifier temperatureModifier();

    @Nullable Registry.BiomeEntry registry();

    enum Precipitation {
        NONE, RAIN, SNOW;
    }

    enum TemperatureModifier {
        NONE, FROZEN;
    }

    interface Setter {
        void setBiome(int x, int y, int z, @NotNull DynamicRegistry.Key<Biome> biome);

        default void setBiome(@NotNull Point blockPosition, @NotNull DynamicRegistry.Key<Biome> biome) {
            setBiome(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), biome);
        }
    }

    interface Getter {
        @NotNull DynamicRegistry.Key<Biome> getBiome(int x, int y, int z);

        default @NotNull DynamicRegistry.Key<Biome> getBiome(@NotNull Point point) {
            return getBiome(point.blockX(), point.blockY(), point.blockZ());
        }
    }

    final class Builder {
        private static final BiomeEffects DEFAULT_EFFECTS = BiomeEffects.builder()
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .waterColor(0x3F76E4)
                .waterFogColor(0x50533)
                .build();

        private float temperature = 0.25f;
        private float downfall = 0.8f;
        private BiomeEffects effects = DEFAULT_EFFECTS;
        private Precipitation precipitation = Precipitation.RAIN;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder downfall(float downfall) {
            this.downfall = downfall;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder effects(@NotNull BiomeEffects effects) {
            this.effects = effects;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder precipitation(@NotNull Biome.Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder temperatureModifier(@NotNull TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        @Contract(pure = true)
        public @NotNull Biome build() {
            return new BiomeImpl(temperature, downfall, effects, precipitation, temperatureModifier, null);
        }
    }
}
