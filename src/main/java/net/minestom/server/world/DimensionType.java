package net.minestom.server.world;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * https://minecraft.wiki/w/Custom_dimension
 */
public sealed interface DimensionType extends ProtocolObject, DimensionTypes permits DimensionTypeImpl {

    int VANILLA_MIN_Y = -64;
    int VANILLA_MAX_Y = 319;

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for dimension types, loading the vanilla dimension types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<DimensionType> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:dimension_type", DimensionTypeImpl.REGISTRY_NBT_TYPE, Registry.Resource.DIMENSION_TYPES,
                (key, props) -> new DimensionTypeImpl(Registry.dimensionType(key, props))
        );
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

    @NotNull String infiniburn();

    @NotNull String effects();

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
        private String effects = "minecraft:overworld";

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder ultrawarm(boolean ultrawarm) {
            this.ultrawarm = ultrawarm;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder natural(boolean natural) {
            this.natural = natural;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder coordinateScale(double coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder hasSkylight(boolean hasSkylight) {
            this.hasSkylight = hasSkylight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder hasCeiling(boolean hasCeiling) {
            this.hasCeiling = hasCeiling;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder fixedTime(@Nullable Long fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder piglinSafe(boolean piglinSafe) {
            this.piglinSafe = piglinSafe;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder bedWorks(boolean bedWorks) {
            this.bedWorks = bedWorks;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder respawnAnchorWorks(boolean respawnAnchorWorks) {
            this.respawnAnchorWorks = respawnAnchorWorks;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder hasRaids(boolean hasRaids) {
            this.hasRaids = hasRaids;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder logicalHeight(int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder minY(int minY) {
            this.minY = minY;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder height(int height) {
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder infiniburn(@NotNull String infiniburn) {
            this.infiniburn = infiniburn;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder effects(@NotNull String effects) {
            this.effects = effects;
            return this;
        }

        @Contract(pure = true)
        public @NotNull DimensionType build() {
            return new DimensionTypeImpl(
                    ultrawarm, natural, coordinateScale, hasSkylight, hasCeiling, ambientLight,
                    fixedTime, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, minY, height,
                    infiniburn, effects, null
            );
        }
    }
}
