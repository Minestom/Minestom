package net.minestom.server.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record DimensionTypeImpl(
        boolean ultrawarm,
        boolean natural,
        double coordinateScale,
        boolean hasSkylight,
        boolean hasCeiling,
        float ambientLight,
        @Nullable Long fixedTime,
        boolean piglinSafe,
        boolean bedWorks,
        boolean respawnAnchorWorks,
        boolean hasRaids,
        int logicalHeight,
        int minY,
        int height,
        @NotNull String infiniburn,
        @NotNull String effects,
        @Nullable Registry.DimensionTypeEntry registry
) implements DimensionType {

    static final BinaryTagSerializer<DimensionType> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("DimensionType is read-only");
            },
            dimensionType -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
                        .putBoolean("ultrawarm", dimensionType.ultrawarm())
                        .putBoolean("natural", dimensionType.natural())
                        .putDouble("coordinate_scale", dimensionType.coordinateScale())
                        .putBoolean("has_skylight", dimensionType.hasSkylight())
                        .putBoolean("has_ceiling", dimensionType.hasCeiling())
                        .putFloat("ambient_light", dimensionType.ambientLight())
                        .putBoolean("piglin_safe", dimensionType.piglinSafe())
                        .putBoolean("bed_works", dimensionType.bedWorks())
                        .putBoolean("respawn_anchor_works", dimensionType.respawnAnchorWorks())
                        .putBoolean("has_raids", dimensionType.hasRaids())
                        .putInt("logical_height", dimensionType.logicalHeight())
                        .putInt("min_y", dimensionType.minY())
                        .putInt("height", dimensionType.height())
                        .putString("infiniburn", dimensionType.infiniburn())
                        .putString("effects", dimensionType.effects())

                        //todo load these from registry
                        .putInt("monster_spawn_block_light_limit", 0)
                        .putInt("monster_spawn_light_level", 0);
                Long fixedTime = dimensionType.fixedTime();
                if (fixedTime != null) builder.putLong("fixed_time", fixedTime);
                return builder.build();
            }
    );

    DimensionTypeImpl(@NotNull Registry.DimensionTypeEntry registry) {
        this(registry.ultrawarm(), registry.natural(), registry.coordinateScale(),
                registry.hasSkylight(), registry.hasCeiling(), registry.ambientLight(), registry.fixedTime(),
                registry.piglinSafe(), registry.bedWorks(), registry.respawnAnchorWorks(), registry.hasRaids(),
                registry.logicalHeight(), registry.minY(), registry.height(), registry.infiniburn(),
                registry.effects(), registry);
    }
}
