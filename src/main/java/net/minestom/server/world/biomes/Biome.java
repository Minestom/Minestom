package net.minestom.server.world.biomes;

import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public final class Biome {
    public static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private static final BiomeEffects DEFAULT_EFFECTS = BiomeEffects.builder()
            .fogColor(0xC0D8FF)
            .skyColor(0x78A7FF)
            .waterColor(0x3F76E4)
            .waterFogColor(0x50533)
            .build();

    //A plains biome has to be registered or else minecraft will crash
    public static final Biome PLAINS = Biome.builder()
            .category(Category.NONE)
            .name(NamespaceID.from("minecraft:plains"))
            .temperature(0.8F)
            .downfall(0.4F)
            .depth(0.125F)
            .scale(0.05F)
            .effects(DEFAULT_EFFECTS)
            .build();

    private final int id = ID_COUNTER.getAndIncrement();

    private final NamespaceID name;
    private final float depth;
    private final float temperature;
    private final float scale;
    private final float downfall;
    private final Category category;
    private final BiomeEffects effects;
    private final Precipitation precipitation;
    private final TemperatureModifier temperatureModifier;

    Biome(NamespaceID name, float depth, float temperature, float scale, float downfall, Category category, BiomeEffects effects, Precipitation precipitation, TemperatureModifier temperatureModifier) {
        this.name = name;
        this.depth = depth;
        this.temperature = temperature;
        this.scale = scale;
        this.downfall = downfall;
        this.category = category;
        this.effects = effects;
        this.precipitation = precipitation;
        this.temperatureModifier = temperatureModifier;
    }

    public static Builder builder() {
        return new Builder();
    }

    public @NotNull NBTCompound toNbt() {
        Check.notNull(name, "The biome namespace cannot be null");
        Check.notNull(effects, "The biome effects cannot be null");

        return NBT.Compound(nbt -> {
            nbt.setString("name", name.toString());
            nbt.setInt("id", id());

            nbt.set("element", NBT.Compound(element -> {
                element.setFloat("depth", depth);
                element.setFloat("temperature", temperature);
                element.setFloat("scale", scale);
                element.setFloat("downfall", downfall);
                element.setString("category", category.name().toLowerCase(Locale.ROOT));
                element.setString("precipitation", precipitation.name().toLowerCase(Locale.ROOT));
                if (temperatureModifier != TemperatureModifier.NONE)
                    element.setString("temperature_modifier", temperatureModifier.name().toLowerCase(Locale.ROOT));
                element.set("effects", effects.toNbt());
            }));
        });
    }

    public int id() {
        return this.id;
    }

    public NamespaceID name() {
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

    public Category category() {
        return this.category;
    }

    public BiomeEffects effects() {
        return this.effects;
    }

    public Precipitation precipitation() {
        return this.precipitation;
    }

    public TemperatureModifier temperatureModifier() {
        return this.temperatureModifier;
    }

    public enum Precipitation {
        RAIN, NONE, SNOW;
    }

    public enum Category {
        NONE, TAIGA, EXTREME_HILLS, JUNGLE, MESA, PLAINS,
        SAVANNA, ICY, THE_END, BEACH, FOREST, OCEAN,
        DESERT, RIVER, SWAMP, MUSHROOM, NETHER;
    }

    public enum TemperatureModifier {
        NONE, FROZEN;
    }

    public static final class Builder {
        private NamespaceID name;
        private float depth = 0.2f;
        private float temperature = 0.25f;
        private float scale = 0.2f;
        private float downfall = 0.8f;
        private Category category = Category.NONE;
        private BiomeEffects effects = DEFAULT_EFFECTS;
        private Precipitation precipitation = Precipitation.RAIN;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        Builder() {
        }

        public Builder name(NamespaceID name) {
            this.name = name;
            return this;
        }

        public Builder depth(float depth) {
            this.depth = depth;
            return this;
        }

        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder downfall(float downfall) {
            this.downfall = downfall;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder effects(BiomeEffects effects) {
            this.effects = effects;
            return this;
        }

        public Builder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public Builder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            return new Biome(name, depth, temperature, scale, downfall, category, effects, precipitation, temperatureModifier);
        }
    }

    public interface Setter {
        void setBiome(int x, int y, int z, @NotNull Biome biome);

        default void setBiome(@NotNull Point blockPosition, @NotNull Biome biome) {
            setBiome(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), biome);
        }
    }

    public interface Getter {
        @NotNull Biome getBiome(int x, int y, int z);

        default @NotNull Biome getBiome(@NotNull Point point) {
            return getBiome(point.blockX(), point.blockY(), point.blockZ());
        }
    }
}
