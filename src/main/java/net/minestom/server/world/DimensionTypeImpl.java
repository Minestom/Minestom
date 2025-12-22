package net.minestom.server.world;

import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.server.world.timeline.Timeline;

record DimensionTypeImpl(
        boolean hasFixedTime,
        boolean hasSkylight,
        boolean hasCeiling,
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
        RegistryTag<Timeline> timelines
) implements DimensionType {
}
