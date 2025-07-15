package net.minestom.server.world;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * https://minecraft.wiki/w/Custom_dimension
 */
public sealed interface DimensionType extends DimensionTypes permits DimensionTypeImpl {

    Key OVERWORLD_EFFECTS = Key.key("minecraft:overworld");

    int VANILLA_MIN_Y = -64;
    int VANILLA_MAX_Y = 319;

    Codec<DimensionType> REGISTRY_CODEC = StructCodec.struct(
            "ultrawarm", Codec.BOOLEAN, DimensionType::ultrawarm,
            "natural", Codec.BOOLEAN, DimensionType::natural,
            "coordinate_scale", Codec.DOUBLE, DimensionType::coordinateScale,
            "has_skylight", Codec.BOOLEAN, DimensionType::hasSkylight,
            "has_ceiling", Codec.BOOLEAN, DimensionType::hasCeiling,
            "ambient_light", Codec.FLOAT, DimensionType::ambientLight,
            "fixed_time", Codec.LONG.optional(), DimensionType::fixedTime,
            "piglin_safe", Codec.BOOLEAN, DimensionType::piglinSafe,
            "bed_works", Codec.BOOLEAN, DimensionType::bedWorks,
            "respawn_anchor_works", Codec.BOOLEAN, DimensionType::respawnAnchorWorks,
            "has_raids", Codec.BOOLEAN, DimensionType::hasRaids,
            "logical_height", Codec.INT, DimensionType::logicalHeight,
            "min_y", Codec.INT, DimensionType::minY,
            "height", Codec.INT, DimensionType::height,
            "infiniburn", Codec.STRING, DimensionType::infiniburn,
            "effects", Codec.KEY.optional(OVERWORLD_EFFECTS), DimensionType::effects,
            "monster_spawn_block_light_limit", Codec.INT, DimensionType::monsterSpawnBlockLightLimit,
            "monster_spawn_light_level", Codec.INT.orElse(Codec.UNIT.transform(ignored -> 0, ignored -> Unit.INSTANCE)), DimensionType::monsterSpawnLightLevel,
            DimensionType::create);

    static DimensionType create(
            boolean ultrawarm, boolean natural, double coordinateScale, boolean hasSkylight, boolean hasCeiling,
            float ambientLight, @Nullable Long fixedTime, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks,
            boolean hasRaids, int logicalHeight, int minY, int height, String infiniburn, Key effects,
            int monsterSpawnBlockLightLimit, int monsterSpawnLightLevel
    ) {
        return new DimensionTypeImpl(ultrawarm, natural, coordinateScale, hasSkylight, hasCeiling, ambientLight,
                fixedTime, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, minY, height,
                infiniburn, effects, monsterSpawnBlockLightLimit, monsterSpawnLightLevel);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for dimension types, loading the vanilla dimension types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<DimensionType> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:dimension_type"), REGISTRY_CODEC, RegistryData.Resource.DIMENSION_TYPES);
    }

    boolean ultrawarm();

    boolean natural();

    double coordinateScale();

    boolean hasSkylight();

    boolean hasCeiling();

    float ambientLight();

    @Nullable Long fixedTime();

    boolean piglinSafe();

    boolean bedWorks();

    boolean respawnAnchorWorks();

    boolean hasRaids();

    int logicalHeight();

    int minY();

    default int maxY() {
        return minY() + height();
    }

    int height();

    String infiniburn();

    Key effects();

    int monsterSpawnBlockLightLimit();

    int monsterSpawnLightLevel();

    default int totalHeight() {
        return minY() + height();
    }

    final class Builder {
        // Defaults match the vanilla overworld
        private boolean ultrawarm = false;
        private boolean natural = true;
        private double coordinateScale = 1.0;
        private boolean hasSkylight = true;
        private boolean hasCeiling = false;
        private float ambientLight = 0f;
        private Long fixedTime = null;
        private boolean piglinSafe = false;
        private boolean bedWorks = true;
        private boolean respawnAnchorWorks = false;
        private boolean hasRaids = true;
        private int logicalHeight = VANILLA_MAX_Y - VANILLA_MIN_Y + 1;
        private int minY = VANILLA_MIN_Y;
        private int height = VANILLA_MAX_Y - VANILLA_MIN_Y + 1;
        private String infiniburn = "#minecraft:infiniburn_overworld";
        private Key effects = OVERWORLD_EFFECTS;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder ultrawarm(boolean ultrawarm) {
            this.ultrawarm = ultrawarm;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder natural(boolean natural) {
            this.natural = natural;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder coordinateScale(double coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder hasSkylight(boolean hasSkylight) {
            this.hasSkylight = hasSkylight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder hasCeiling(boolean hasCeiling) {
            this.hasCeiling = hasCeiling;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder fixedTime(@Nullable Long fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder piglinSafe(boolean piglinSafe) {
            this.piglinSafe = piglinSafe;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder bedWorks(boolean bedWorks) {
            this.bedWorks = bedWorks;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder respawnAnchorWorks(boolean respawnAnchorWorks) {
            this.respawnAnchorWorks = respawnAnchorWorks;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder hasRaids(boolean hasRaids) {
            this.hasRaids = hasRaids;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder logicalHeight(int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder minY(int minY) {
            this.minY = minY;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder infiniburn(String infiniburn) {
            this.infiniburn = infiniburn;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder effects(@KeyPattern String effects) {
            return effects(Key.key(effects));
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder effects(Key effects) {
            this.effects = effects;
            return this;
        }

        @Contract(pure = true)
        public DimensionType build() {
            return new DimensionTypeImpl(
                    ultrawarm, natural, coordinateScale, hasSkylight, hasCeiling, ambientLight,
                    fixedTime, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, minY, height,
                    infiniburn, effects, 0, 0
            );
        }
    }
}
