package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public abstract class Heightmap {
    private final short[] heights = new short[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final Chunk chunk;
    private final int minHeight;
    private boolean needsRefresh = true;

    public Heightmap(Chunk chunk) {
        this.chunk = chunk;
        minHeight = chunk.getInstance().getCachedDimensionType().minY() - 1;
    }

    protected abstract boolean checkBlock(@NotNull Block block);

    public abstract String NBTName();

    public void refresh(int x, int y, int z, Block block) {
        if (checkBlock(block)) {
            if (getHeight(x, z) < y) {
                setHeightY(x, z, y);
            }
        } else if (y == getHeight(x, z)) {
            refresh(x, z, y - 1);
        }
    }

    public void refresh(int startY) {
        if (!needsRefresh) return;

        synchronized (chunk) {
            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    refresh(x, z, startY);
                }
            }
        }
        needsRefresh = false;
    }

    public void refresh(int x, int z, int startY) {
        int y = startY;
        while (y > minHeight) {
            Block block = chunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (checkBlock(block)) break;
            y--;
        }
        setHeightY(x, z, y);
    }

    public long[] getNBT() {
        final int dimensionHeight = chunk.getInstance().getCachedDimensionType().height();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return encode(heights, bitsForHeight);
    }

    public void loadFrom(long[] data) {
        final int dimensionHeight = chunk.getInstance().getCachedDimensionType().height();
        final int bitsPerEntry = MathUtils.bitsToRepresent(dimensionHeight);

        final int entriesPerLong = 64 / bitsPerEntry;

        final int maxPossibleIndexInContainer = entriesPerLong - 1;
        final int entryMask = (1 << bitsPerEntry) - 1;

        int containerIndex = 0;
        for (int i = 0; i < heights.length; i++) {
            final int indexInContainer = i % entriesPerLong;

            heights[i] = (short) ((int) (data[containerIndex] >> (indexInContainer * bitsPerEntry)) & entryMask);

            if (indexInContainer == maxPossibleIndexInContainer) containerIndex++;
        }

        needsRefresh = false;
    }

    // highest breaking block in section
    public int getHeight(int x, int z) {
        if (needsRefresh) refresh(getHighestBlockSection(chunk));
        return heights[z << 4 | x] + minHeight;
    }

    private void setHeightY(int x, int z, int height) {
        heights[z << 4 | x] = (short) (height - minHeight);
    }

    public static int getHighestBlockSection(Chunk chunk) {
        int y = chunk.getInstance().getCachedDimensionType().maxY();

        final int sectionsCount = chunk.getMaxSection() - chunk.getMinSection();
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
     * @param heights      array of heights. Note that for this method it doesn't matter what size this array will be.
     *                     But to get correct heights, array must be 256 elements long, and at index `i` must be height of (z=i/16, x=i%16).
     * @param bitsPerEntry bits that each entry from height will take in `long` container.
     * @return array of encoded heights.
     */
    static long[] encode(short[] heights, int bitsPerEntry) {
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
