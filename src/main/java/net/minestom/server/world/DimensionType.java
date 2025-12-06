package net.minestom.server.world;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.server.world.timeline.Timeline;
import org.jetbrains.annotations.ApiStatus;

/**
 * https://minecraft.wiki/w/Custom_dimension
 */
public sealed interface DimensionType extends DimensionTypes permits DimensionTypeImpl {
    int VANILLA_MIN_Y = -64;
    int VANILLA_MAX_Y = 319;

    Codec<DimensionType> REGISTRY_CODEC = StructCodec.struct(
            "has_fixed_time", Codec.BOOLEAN.optional(false), DimensionType::hasFixedTime,
            "has_skylight", Codec.BOOLEAN, DimensionType::hasSkylight,
            "has_ceiling", Codec.BOOLEAN, DimensionType::hasCeiling,
            "coordinate_scale", Codec.DOUBLE, DimensionType::coordinateScale,
            "min_y", Codec.INT, DimensionType::minY,
            "height", Codec.INT, DimensionType::height,
            "logical_height", Codec.INT, DimensionType::logicalHeight,
            "infiniburn", Codec.STRING, DimensionType::infiniburn,
            "ambient_light", Codec.FLOAT, DimensionType::ambientLight,
            "monster_spawn_light_level", IntProvider.CODEC, DimensionType::monsterSpawnLightLevel,
            "monster_spawn_block_light_limit", Codec.INT, DimensionType::monsterSpawnBlockLightLimit,
            "skybox", Skybox.CODEC.optional(Skybox.OVERWORLD), DimensionType::skybox,
            "cardinal_light", CardinalLight.CODEC.optional(CardinalLight.DEFAULT), DimensionType::cardinalLight,
            "attributes", EnvironmentAttributeMap.CODEC.optional(EnvironmentAttributeMap.EMPTY), DimensionType::attributes,
            "timelines", RegistryTag.codec(Registries::timeline).optional(RegistryTag.empty()), DimensionType::timelines,
            DimensionType::create);

    static DimensionType create(
            boolean hasFixedTime, boolean hasSkyLight, boolean hasCeiling,
            double coordinateScale, int minY, int height, int logicalHeight,
            String infiniburn, float ambientLight,
            IntProvider monsterSpawnLightLevel, int monsterSpawnBlockLightLimit,
            Skybox skybox, CardinalLight cardinalLight,
            EnvironmentAttributeMap attributes, RegistryTag<Timeline> timelines
    ) {
        return new DimensionTypeImpl(hasFixedTime, hasSkyLight, hasCeiling,
                coordinateScale, minY, height, logicalHeight, infiniburn,
                ambientLight, monsterSpawnLightLevel, monsterSpawnBlockLightLimit,
                skybox, cardinalLight, attributes, timelines
        );
    }

//    static Builder builder() {
//        return new Builder();
//    }

    /**
     * <p>Creates a new registry for dimension types, loading the vanilla dimension types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<DimensionType> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("dimension_type"),
                REGISTRY_CODEC, RegistryData.Resource.DIMENSION_TYPES);
    }

    boolean hasFixedTime();

    boolean hasSkylight();

    boolean hasCeiling();

    double coordinateScale();

    int minY();

    default int maxY() {
        return minY() + height();
    }

    int height();

    int logicalHeight();

    String infiniburn();

    float ambientLight();

    IntProvider monsterSpawnLightLevel();

    int monsterSpawnBlockLightLimit();

    Skybox skybox();

    CardinalLight cardinalLight();

    EnvironmentAttributeMap attributes();

    RegistryTag<Timeline> timelines();

    default int totalHeight() {
        return minY() + height();
    }

    enum Skybox {
        NONE,
        OVERWORLD,
        END;

        public static final Codec<Skybox> CODEC = Codec.Enum(Skybox.class);
    }

    enum CardinalLight {
        DEFAULT,
        NETHER;

        public static final Codec<CardinalLight> CODEC = Codec.Enum(CardinalLight.class);
    }

    //TODO
//    final class Builder {
//        // Defaults match the vanilla overworld
//        private boolean ultrawarm = false;
//        private boolean natural = true;
//        private double coordinateScale = 1.0;
//        private boolean hasSkylight = true;
//        private boolean hasCeiling = false;
//        private float ambientLight = 0f;
//        private @Nullable Long fixedTime = null;
//        private boolean piglinSafe = false;
//        private boolean bedWorks = true;
//        private boolean respawnAnchorWorks = false;
//        private boolean hasRaids = true;
//        private int logicalHeight = VANILLA_MAX_Y - VANILLA_MIN_Y + 1;
//        private @Nullable Integer cloudHeight = null; // Will match vanilla default (192) in next
//        private int minY = VANILLA_MIN_Y;
//        private int height = VANILLA_MAX_Y - VANILLA_MIN_Y + 1;
//        private String infiniburn = "#minecraft:infiniburn_overworld";
//        private Key effects = OVERWORLD_EFFECTS;
//
//        private Builder() {
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder ultrawarm(boolean ultrawarm) {
//            this.ultrawarm = ultrawarm;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder natural(boolean natural) {
//            this.natural = natural;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder coordinateScale(double coordinateScale) {
//            this.coordinateScale = coordinateScale;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder hasSkylight(boolean hasSkylight) {
//            this.hasSkylight = hasSkylight;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder hasCeiling(boolean hasCeiling) {
//            this.hasCeiling = hasCeiling;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder ambientLight(float ambientLight) {
//            this.ambientLight = ambientLight;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder fixedTime(@Nullable Long fixedTime) {
//            this.fixedTime = fixedTime;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder piglinSafe(boolean piglinSafe) {
//            this.piglinSafe = piglinSafe;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder bedWorks(boolean bedWorks) {
//            this.bedWorks = bedWorks;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder respawnAnchorWorks(boolean respawnAnchorWorks) {
//            this.respawnAnchorWorks = respawnAnchorWorks;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder hasRaids(boolean hasRaids) {
//            this.hasRaids = hasRaids;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder logicalHeight(int logicalHeight) {
//            this.logicalHeight = logicalHeight;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder cloudHeight(@Nullable Integer cloudHeight) {
//            this.cloudHeight = cloudHeight;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder minY(int minY) {
//            this.minY = minY;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder height(int height) {
//            this.height = height;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder infiniburn(String infiniburn) {
//            this.infiniburn = infiniburn;
//            return this;
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder effects(@KeyPattern String effects) {
//            return effects(Key.key(effects));
//        }
//
//        @Contract(value = "_ -> this", pure = true)
//        public Builder effects(Key effects) {
//            this.effects = effects;
//            return this;
//        }
//
//        @Contract(pure = true)
//        public DimensionType build() {
//            return new DimensionTypeImpl(
//                    ultrawarm, natural, coordinateScale, hasSkylight, hasCeiling, ambientLight,
//                    fixedTime, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, cloudHeight, minY, height,
//                    infiniburn, effects, 0, 0
//            );
//        }
//    }
}
