package net.minestom.server.world.biome;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import org.jetbrains.annotations.ApiStatus;

public sealed interface Biome extends Biomes permits BiomeImpl {
    Codec<Biome> REGISTRY_CODEC = StructCodec.struct(
            "has_precipitation", Codec.BOOLEAN, Biome::hasPrecipitation,
            "temperature", Codec.FLOAT, Biome::temperature,
            "temperature_modifier", TemperatureModifier.CODEC.optional(TemperatureModifier.NONE), Biome::temperatureModifier,
            "downfall", Codec.FLOAT, Biome::downfall,
            "attributes", EnvironmentAttributeMap.CODEC.optional(EnvironmentAttributeMap.EMPTY), Biome::attributes,
            "effects", BiomeEffects.CODEC, Biome::effects,
            Biome::create);
    // We dont currently read generation or mob spawn settings. If we do, we will need
    // to have a separate network codec which does not serialize those fields.
    Codec<Biome> NETWORK_CODEC = REGISTRY_CODEC;

    static Biome create(
            boolean hasPrecipitation,
            float temperature,
            TemperatureModifier temperatureModifier,
            float downfall,
            EnvironmentAttributeMap attributes,
            BiomeEffects effects
    ) {
        return new BiomeImpl(hasPrecipitation, temperature, temperatureModifier, downfall, attributes, effects);
    }

//    static Builder builder() {
//        return new Builder();
//    }

    /**
     * <p>Creates a new registry for biomes, loading the vanilla trim biomes.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<Biome> createDefaultRegistry() {
        return DynamicRegistry.create(
                Key.key("worldgen/biome"), NETWORK_CODEC, null, RegistryData.Resource.BIOMES,
                // We force plains to be first because it allows convenient palette initialization.
                // Maybe worth switching to fetching plains in the palette in the future to avoid this.
                (a, b) -> a.equals("minecraft:plains") ? -1 : b.equals("minecraft:plains") ? 1 : 0,
                REGISTRY_CODEC
        );
    }

    boolean hasPrecipitation();

    float temperature();

    TemperatureModifier temperatureModifier();

    float downfall();

    EnvironmentAttributeMap attributes();

    BiomeEffects effects();


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

    enum TemperatureModifier {
        NONE, FROZEN;

        public static final Codec<TemperatureModifier> CODEC = Codec.Enum(TemperatureModifier.class);
    }

//    final class Builder {
//        private boolean hasPrecipitation;
//        private float temperature;
//        private TemperatureModifier temperatureModifier;
//        private float downfall;
//        private EnvironmentAttributeMap attributes;
//        private BiomeEffects effects;
//
//        private Builder() {
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder temperature(float temperature) {
//            this.temperature = temperature;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder downfall(float downfall) {
//            this.downfall = downfall;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder effects(BiomeEffects effects) {
//            this.effects = effects;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder hasPrecipitation(boolean precipitation) {
//            this.hasPrecipitation = precipitation;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
//            this.temperatureModifier = temperatureModifier;
//            return this;
//        }
//
//        @Contract(pure = true)
//        public Biome build() {
//            return new BiomeImpl(temperature, downfall, effects, hasPrecipitation, temperatureModifier);
//        }
//    }
}
