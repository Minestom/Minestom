package net.minestom.server.world.biome;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.BuiltinRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface Biome extends Biomes permits BiomeImpl {
    @NotNull Codec<Biome> REGISTRY_CODEC = StructCodec.struct(
            "temperature", Codec.FLOAT, Biome::temperature,
            "downfall", Codec.FLOAT, Biome::downfall,
            "has_precipitation", Codec.BOOLEAN, Biome::hasPrecipitation,
            "temperature_modifier", TemperatureModifier.CODEC.optional(TemperatureModifier.NONE), Biome::temperatureModifier,
            "effects", BiomeEffects.CODEC.optional(BiomeEffects.PLAINS_EFFECTS), Biome::effects,
            Biome::create);
    @NotNull Codec<Biome> NETWORK_CODEC = StructCodec.struct(
            "temperature", Codec.FLOAT, Biome::temperature,
            "downfall", Codec.FLOAT, Biome::downfall,
            "has_precipitation", Codec.BOOLEAN, Biome::hasPrecipitation,
            "temperature_modifier", TemperatureModifier.CODEC, Biome::temperatureModifier,
            "effects", BiomeEffects.CODEC, Biome::effects,
            Biome::create);

    static @NotNull Biome create(float temperature, float downfall, boolean hasPrecipitation,
                                 @NotNull TemperatureModifier temperatureModifier, @NotNull BiomeEffects effects) {
        return new BiomeImpl(temperature, downfall, effects, hasPrecipitation, temperatureModifier);
    }

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
        return DynamicRegistry.load(
                BuiltinRegistries.WORLDGEN_BIOME, NETWORK_CODEC, null,
                // We force plains to be first because it allows convenient palette initialization.
                // Maybe worth switching to fetching plains in the palette in the future to avoid this.
                (a, b) -> a.equals(PLAINS) ? -1 : b.equals(PLAINS) ? 1 : 0,
                REGISTRY_CODEC
        );
    }

    float temperature();

    float downfall();

    @NotNull BiomeEffects effects();

    boolean hasPrecipitation();

    @NotNull TemperatureModifier temperatureModifier();

    enum TemperatureModifier {
        NONE, FROZEN;

        public static final Codec<TemperatureModifier> CODEC = Codec.Enum(TemperatureModifier.class);
    }

    interface Setter {
        void setBiome(int x, int y, int z, @NotNull RegistryKey<Biome> biome);

        default void setBiome(@NotNull Point blockPosition, @NotNull RegistryKey<Biome> biome) {
            setBiome(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), biome);
        }
    }

    interface Getter {
        @NotNull RegistryKey<Biome> getBiome(int x, int y, int z);

        default @NotNull RegistryKey<Biome> getBiome(@NotNull Point point) {
            return getBiome(point.blockX(), point.blockY(), point.blockZ());
        }
    }

    final class Builder {
        private float temperature = 0.25f;
        private float downfall = 0.8f;
        private BiomeEffects effects = BiomeEffects.PLAINS_EFFECTS;
        private boolean hasPrecipitation = false;
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
        public @NotNull Builder hasPrecipitation(boolean precipitation) {
            this.hasPrecipitation = precipitation;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder temperatureModifier(@NotNull TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        @Contract(pure = true)
        public @NotNull Biome build() {
            return new BiomeImpl(temperature, downfall, effects, hasPrecipitation, temperatureModifier);
        }
    }
}
