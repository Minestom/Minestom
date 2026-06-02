package net.minestom.server.world;

import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.server.world.clock.WorldClock;
import net.minestom.server.world.timeline.Timeline;
import org.jetbrains.annotations.Nullable;

record DimensionTypeImpl(
        boolean hasFixedTime,
        boolean hasSkylight,
        boolean hasCeiling,
        boolean hasEnderDragonFight,
        double coordinateScale,
        int minY,
        int height,
        int logicalHeight,
        String infiniburn,
        float ambientLight,
        IntProvider monsterSpawnLightLevel,
        int monsterSpawnBlockLightLimit,
        Skybox skybox,
        CardinalLight cardinalLight,
        EnvironmentAttributeMap attributes,
        RegistryTag<Timeline> timelines,
        @Nullable RegistryKey<WorldClock> defaultClock
) implements DimensionType {
}
