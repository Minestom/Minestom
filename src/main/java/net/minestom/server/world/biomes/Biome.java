package net.minestom.server.world.biomes;

import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.concurrent.atomic.AtomicInteger;

public class Biome {

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
    private final TemperatureModifier temperature_modifier;

    Biome(NamespaceID name, float depth, float temperature, float scale, float downfall, Category category, BiomeEffects effects, Precipitation precipitation, TemperatureModifier temperature_modifier) {
        this.name = name;
        this.depth = depth;
        this.temperature = temperature;
        this.scale = scale;
        this.downfall = downfall;
        this.category = category;
        this.effects = effects;
        this.precipitation = precipitation;
        this.temperature_modifier = temperature_modifier;
    }

    public static BiomeBuilder builder() {
        return new BiomeBuilder();
    }

    @NotNull
    public NBTCompound toNbt() {
        Check.notNull(name, "The biome namespace cannot be null");
        Check.notNull(effects, "The biome effects cannot be null");

        NBTCompound nbt = new NBTCompound();
        nbt.setString("name", name.toString());
        nbt.setInt("id", getId());

        NBTCompound element = new NBTCompound();
        element.setFloat("depth", depth);
        element.setFloat("temperature", temperature);
        element.setFloat("scale", scale);
        element.setFloat("downfall", downfall);
        element.setString("category", category.getType());
        element.setString("precipitation", precipitation.getType());
        if (temperature_modifier != TemperatureModifier.NONE)
            element.setString("temperature_modifier", temperature_modifier.getType());
        element.set("effects", effects.toNbt());
        nbt.set("element", element);
        return nbt;
    }

    public int getId() {
        return this.id;
    }

    public NamespaceID getName() {
        return this.name;
    }

    public float getDepth() {
        return this.depth;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public float getScale() {
        return this.scale;
    }

    public float getDownfall() {
        return this.downfall;
    }

    public Category getCategory() {
        return this.category;
    }

    public BiomeEffects getEffects() {
        return this.effects;
    }

    public Precipitation getPrecipitation() {
        return this.precipitation;
    }

    public TemperatureModifier getTemperature_modifier() {
        return this.temperature_modifier;
    }

    public enum Precipitation {
        RAIN("rain"), NONE("none"), SNOW("snow");

        String type;

        Precipitation(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public enum Category {
        NONE("none"), TAIGA("taiga"), EXTREME_HILLS("extreme_hills"), JUNGLE("jungle"), MESA("mesa"), PLAINS("plains"),
        SAVANNA("savanna"), ICY("icy"), THE_END("the_end"), BEACH("beach"), FOREST("forest"), OCEAN("ocean"),
        DESERT("desert"), RIVER("river"), SWAMP("swamp"), MUSHROOM("mushroom"), NETHER("nether");

        String type;

        Category(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public enum TemperatureModifier {
        NONE("none"), FROZEN("frozen");

        String type;

        TemperatureModifier(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static int getBiomeCount(DimensionType dimensionType) {
        final int height = dimensionType.getLogicalHeight();
        return 4 * 4 * 4 * (height / Chunk.CHUNK_SECTION_SIZE);
    }

    public static class BiomeBuilder {

        private NamespaceID name;
        private float depth = 0.2f;
        private float temperature = 0.25f;
        private float scale = 0.2f;
        private float downfall = 0.8f;
        private Category category = Category.NONE;
        private BiomeEffects effects = DEFAULT_EFFECTS;
        private Precipitation precipitation = Precipitation.RAIN;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        BiomeBuilder() {
        }

        public Biome.BiomeBuilder name(NamespaceID name) {
            this.name = name;
            return this;
        }

        public Biome.BiomeBuilder depth(float depth) {
            this.depth = depth;
            return this;
        }

        public Biome.BiomeBuilder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        public Biome.BiomeBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Biome.BiomeBuilder downfall(float downfall) {
            this.downfall = downfall;
            return this;
        }

        public Biome.BiomeBuilder category(Category category) {
            this.category = category;
            return this;
        }

        public Biome.BiomeBuilder effects(BiomeEffects effects) {
            this.effects = effects;
            return this;
        }

        public Biome.BiomeBuilder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public Biome.BiomeBuilder temperatureModifier(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            return new Biome(name, depth, temperature, scale, downfall, category, effects, precipitation, temperatureModifier);
        }
    }
}
