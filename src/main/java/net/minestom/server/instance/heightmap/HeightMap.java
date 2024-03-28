package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public interface HeightMap {
    NBTCompound getNBT();
    void refresh();
    void refreshAt(int x, int z);

    static int getStartY(Chunk chunk) {
        int y = chunk.getInstance().getDimensionType().getMaxY();

        final int sectionsCount = chunk.getSections().size();
        for (int i = 0; i < sectionsCount; i++) {
            int sectionY = chunk.getMaxSection() - i - 1;
            var blockPalette = chunk.getSection(sectionY).blockPalette();
            if (blockPalette.count() != 0) break;
            y -= 16;
        }
        return y;
    }

    /**
     * Creates compressed longs array from uncompressed heights array.
     *
     * @param heights array of heights. Note that for this method it doesn't matter what size this array will be.
     * But to get correct heights, array must be 256 elements long, and at index `i` must be height of (z=i/16, x=i%16).
     * @param bitsPerEntry bits that each entry from height will take in `long` container.
     * @return array of encoded heights.
     */
    static long[] encode(int[] heights, int bitsPerEntry) {
        final int entriesPerLong = 64 / bitsPerEntry;
        // ceil(HeightsCount / entriesPerLong)
        final int len = (heights.length + entriesPerLong - 1) / entriesPerLong;

        final int maxPossibleIndexInContainer = entriesPerLong - 1;
        final int entryMask = (1 << bitsPerEntry) - 1;

        long[] data = new long[len];
        int containerIndex = 0;
        for (int i = 0; i < heights.length; i++) {
            final int indexInContainer = i % entriesPerLong;
            final int entry = heights[i];

            data[containerIndex] |= ((long) (entry & entryMask)) << (indexInContainer * bitsPerEntry);

            if (indexInContainer == maxPossibleIndexInContainer) containerIndex++;
        }

        return data;
    }
}
