package net.minestom.server.world;

import net.kyori.adventure.key.Key;
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
        @NotNull Key effects,
        int monsterSpawnBlockLightLimit
) implements DimensionType {
}
