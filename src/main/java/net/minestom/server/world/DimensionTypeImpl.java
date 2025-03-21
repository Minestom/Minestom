package net.minestom.server.world;

import net.minestom.server.registry.Registry;
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
        int monsterSpawnBlockLightLimit,
        int monsterSpawnLightLevel,
        @Nullable Registry.DimensionTypeEntry registry
) implements DimensionType {

    DimensionTypeImpl(@NotNull Registry.DimensionTypeEntry registry) {
        this(registry.ultrawarm(), registry.natural(), registry.coordinateScale(),
                registry.hasSkylight(), registry.hasCeiling(), registry.ambientLight(), registry.fixedTime(),
                registry.piglinSafe(), registry.bedWorks(), registry.respawnAnchorWorks(), registry.hasRaids(),
                registry.logicalHeight(), registry.minY(), registry.height(), registry.infiniburn(),
                registry.effects(), 0, 0, registry);
    }
}
