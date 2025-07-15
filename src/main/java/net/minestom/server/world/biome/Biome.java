package net.minestom.server.world.biome;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

public sealed interface Biome extends Biomes permits BiomeImpl {
    Codec<Biome> REGISTRY_CODEC = StructCodec.struct(
            "temperature", Codec.FLOAT, Biome::temperature,
            "downfall", Codec.FLOAT, Biome::downfall,
            "has_precipitation", Codec.BOOLEAN, Biome::hasPrecipitation,
            "temperature_modifier", TemperatureModifier.CODEC.optional(TemperatureModifier.NONE), Biome::temperatureModifier,
            "effects", BiomeEffects.CODEC.optional(BiomeEffects.PLAINS_EFFECTS), Biome::effects,
            Biome::create);
    Codec<Biome> NETWORK_CODEC = StructCodec.struct(
            "temperature", Codec.FLOAT, Biome::temperature,
            "downfall", Codec.FLOAT, Biome::downfall,
            "has_precipitation", Codec.BOOLEAN, Biome::hasPrecipitation,
            "temperature_modifier", TemperatureModifier.CODEC, Biome::temperatureModifier,
            "effects", BiomeEffects.CODEC, Biome::effects,
            Biome::create);

    static Biome create(float temperature, float downfall, boolean hasPrecipitation,
                                 TemperatureModifier temperatureModifier, BiomeEffects effects) {
        return new BiomeImpl(temperature, downfall, effects, hasPrecipitation, temperatureModifier);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for biomes, loading the vanilla trim biomes.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<Biome> createDefaultRegistry() {
        return DynamicRegistry.create(
                Key.key("minecraft:worldgen/biome"), NETWORK_CODEC, null, RegistryData.Resource.BIOMES,
                // We force plains to be first because it allows convenient palette initialization.
                // Maybe worth switching to fetching plains in the palette in the future to avoid this.
                (a, b) -> a.equals("minecraft:plains") ? -1 : b.equals("minecraft:plains") ? 1 : 0,
                REGISTRY_CODEC
        );
    }

    float temperature();

    float downfall();

    BiomeEffects effects();

    boolean hasPrecipitation();

    TemperatureModifier temperatureModifier();

    enum TemperatureModifier {
        NONE, FROZEN;

        public static final Codec<TemperatureModifier> CODEC = Codec.Enum(TemperatureModifier.class);
    }

    interface Setter {
        void setBiome(int x, int y, int z, RegistryKey<Biome> biome);

        default void setBiome(Point blockPosition, RegistryKey<Biome> biome) {
            setBiome(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), biome);
        }
    }

    interface Getter {
        RegistryKey<Biome> getBiome(int x, int y, int z);

        default RegistryKey<Biome> getBiome(Point point) {
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
        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder downfall(float downfall) {
            this.downfall = downfall;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder effects(BiomeEffects effects) {
            this.effects = effects;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder hasPrecipitation(boolean precipitation) {
            this.hasPrecipitation = precipitation;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        @Contract(pure = true)
        public Biome build() {
            return new BiomeImpl(temperature, downfall, effects, hasPrecipitation, temperatureModifier);
        }
    }
}
