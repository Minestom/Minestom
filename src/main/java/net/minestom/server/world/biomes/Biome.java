package net.minestom.server.world.biomes;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

sealed public interface Biome extends ProtocolObject permits BiomeImpl {
    /**
     * Returns the entity registry.
     *
     * @return the entity registry or null if it was created with a builder
     */
    @Contract(pure = true)
    @Nullable Registry.BiomeEntry registry();

    @Override
    @NotNull NamespaceID namespace();
    float temperature();
    float downfall();
    @NotNull BiomeEffects effects();
    @NotNull Precipitation precipitation();
    @NotNull TemperatureModifier temperatureModifier();

    enum Precipitation {
        NONE, RAIN, SNOW;
    }

    enum TemperatureModifier {
        NONE, FROZEN;
    }

    interface Setter {
        void setBiome(int x, int y, int z, @NotNull Biome biome);

        default void setBiome(@NotNull Point blockPosition, @NotNull Biome biome) {
            setBiome(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), biome);
        }
    }

    interface Getter {
        @NotNull Biome getBiome(int x, int y, int z);

        default @NotNull Biome getBiome(@NotNull Point point) {
            return getBiome(point.blockX(), point.blockY(), point.blockZ());
        }
    }

    default @NotNull RegistryDataPacket.Entry toRegistryEntry() {
        return new RegistryDataPacket.Entry(namespace().toString(), toNbt());
    }

    default @NotNull CompoundBinaryTag toNbt() {
        Check.notNull(name(), "The biome namespace cannot be null");
        Check.notNull(effects(), "The biome effects cannot be null");

        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
                .putFloat("temperature", temperature())
                .putFloat("downfall", downfall())
                .putByte("has_precipitation", (byte) (precipitation() == Precipitation.NONE ? 0 : 1))
                .putString("precipitation", precipitation().name().toLowerCase(Locale.ROOT));
        if (temperatureModifier() != TemperatureModifier.NONE)
            builder.putString("temperature_modifier", temperatureModifier().name().toLowerCase(Locale.ROOT));
        return builder
                .put("effects", effects().toNbt())
                .build();
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private static final BiomeEffects DEFAULT_EFFECTS = BiomeEffects.builder()
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .waterColor(0x3F76E4)
                .waterFogColor(0x50533)
                .build();

        private NamespaceID name;
        private float temperature = 0.25f;
        private float downfall = 0.8f;
        private BiomeEffects effects = DEFAULT_EFFECTS;
        private Precipitation precipitation = Precipitation.RAIN;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder name(@NotNull NamespaceID name) {
            this.name = name;
            return this;
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
            return new BiomeImpl(name, temperature, downfall, effects, precipitation, temperatureModifier);
        }
    }
}
