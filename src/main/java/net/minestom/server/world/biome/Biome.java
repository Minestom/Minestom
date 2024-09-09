package net.minestom.server.world.biome;

import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Biome extends Biomes, ProtocolObject permits BiomeImpl {

    static @NotNull Builder builder(NamespaceID namespace) {
        return new Builder(namespace);
    }

    /**
     * <p>Creates a new registry for biomes, loading the vanilla trim biomes.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Biome> createDefaultRegistry() {
        List<Biome> biomes = new java.util.ArrayList<>(Registry.loadRegistry(Registry.Resource.BIOMES, Registry.BiomeEntry::new).stream()
                .<Biome>map(BiomeImpl::new).toList());
        // We force plains to be first because it allows convenient palette initialization.
        // Maybe worth switching to fetching plains in the palette in the future to avoid this.
        final NamespaceID plains = NamespaceID.from("minecraft", "plains");
        biomes.sort((a, b) -> a.registry().namespace().equals(plains) ? -1 : b.registry().namespace().equals(plains) ? 1 : 0);
        return DynamicRegistry.create("minecraft:worldgen/biome", BiomeImpl.REGISTRY_NBT_TYPE, biomes);
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

        private final NamespaceID namespace;
        private float temperature = 0.25f;
        private float downfall = 0.8f;
        private BiomeEffects effects = DEFAULT_EFFECTS;
        private Precipitation precipitation = Precipitation.RAIN;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        private Builder(NamespaceID namespace) {
            this.namespace = namespace;
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
            return new BiomeImpl(namespace, temperature, downfall, effects, precipitation, temperatureModifier, null);
        }
    }
}
