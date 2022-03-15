package net.minestom.server.instance.light.starlight;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

public class LightWorldUtil {

    public static int getMinSection(Instance world) {
        return ChunkUtils.getChunkCoordinate(world.getDimensionType().getMinY());
    }

    public static int getMaxSection(Instance world) {
        return ChunkUtils.getChunkCoordinate(world.getDimensionType().getMaxY());
    }

    public static int getMinLightSection(Instance world) {
        return getMinSection(world) - 1;
    }

    public static int getMaxLightSection(Instance world) {
        return getMaxSection(world) + 1;
    }

    public static int getTotalSections(Instance world) {
        return getMaxSection(world) - getMinSection(world) + 1;
    }

    public static int getTotalLightSections(Instance world) {
        return getMaxLightSection(world) - getMinLightSection(world) + 1;
    }

    private LightWorldUtil() {}

}
