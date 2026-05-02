package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.instance.light.LightSection.LightUpdateResult;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;

public class SkyLightSection {
    private final LightSection section;

    public SkyLightSection(LightSection section) {
        this.section = section;
    }

    LightUpdateResult<byte[]> relightSkyLightInternal() {
        var version = section.getNextSkyLightInternalVersion();
        var lightData = prepareSkyLightInternal(version);
        return section.updateSkyLightInternal(lightData);
    }

    LightUpdateResult<byte[]> relightSkyLightExternal() {
        var version = section.getNextSkyLightExternalVersion();
        return section.updateSkyLightExternal(new LightSection.LightData<>(LightCompute.EMPTY_CONTENT, version));
    }

    private LightSection.LightData<byte[]> prepareSkyLightInternal(int version) {
        if (section.chunkSection == null)
            return new LightSection.LightData<>(LightCompute.EMPTY_CONTENT, version);
        Palette blockPalette;
        int[] heightmap;
        section.chunk.lockReadLock();
        try {
            blockPalette = section.chunkSection.blockPalette().clone();
            heightmap = section.chunk.getOcclusionMap().clone();
        } finally {
            section.chunk.unlockReadLock();
        }
        var maxY = section.chunk.getInstance().getCachedDimensionType().maxY();
        var sectionY = section.sectionY();
        var queue = getSkyLightInternalSources(heightmap, maxY, sectionY);
        var content = queue == null ? LightCompute.CONTENT_FULLY_LIT : LightCompute.compute(blockPalette, queue);
        return new LightSection.LightData<>(content, version);
    }

    private static @Nullable ShortArrayFIFOQueue getSkyLightInternalSources(int[] heightmap, int maxY, int sectionY) {
        final int sectionMaxY = (sectionY + 1) * 16 - 1;
        final int sectionMinY = sectionY * 16;
        var fullyLit = true;
        for (var i = 0; i < 16 * 16; i++) {
            final int height = heightmap[i];
            if (height > sectionMinY) {
                fullyLit = false;
                break;
            }
        }
        if (fullyLit) return null;

        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int height = heightmap[z << 4 | x];
                var topmostY = Math.min(sectionMaxY, maxY);
                var endY = Math.max(height, sectionMinY);
                for (int y = topmostY; y >= endY; y--) {
                    final int index = x | (z << 4) | ((y % 16) << 8);
                    lightSources.enqueue((short) (index | (15 << 12)));
                }
            }
        }
        return lightSources;
    }
}
