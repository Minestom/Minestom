package net.minestom.server.world.biome;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

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

    final class Builder {
        private boolean hasPrecipitation = true;
        private float temperature = 0.8f;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        private float downfall = 0.4f;
        private EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder();
        private BiomeEffects effects = BiomeEffects.DEFAULT;

        private Builder() {
        }

        @Contract(value = "_ -> this")
        public Builder precipitation(boolean hasPrecipitation) {
            this.hasPrecipitation = hasPrecipitation;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder downfall(float downfall) {
            this.downfall = downfall;
            return this;
        }

        @Contract(value = "_, _ -> this")
        public <T> Builder setAttribute(EnvironmentAttribute<T> attribute, T value) {
            attributes.set(attribute, value);
            return this;
        }

        @Contract(value = "_, _, _ -> this")
        public <T, Arg> Builder modifyAttribute(EnvironmentAttribute<T> attribute, EnvironmentAttribute.Modifier<T, Arg> modifier, Arg argument) {
            attributes.modify(attribute, modifier, argument);
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder effects(BiomeEffects effects) {
            this.effects = effects;
            return this;
        }

        @Contract(pure = true)
        public Biome build() {
            return Biome.create(hasPrecipitation, temperature, temperatureModifier, downfall, attributes.build(), effects);
        }

    }
}
