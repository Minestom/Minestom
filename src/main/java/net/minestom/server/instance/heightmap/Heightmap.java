package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableLongArray;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTLongArray;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public abstract class Heightmap {
    private final int[] heights = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];

    private final Chunk attachedChunk;

    private final int minHeight;

    public Heightmap(Chunk attachedChunk) {
        this.attachedChunk = attachedChunk;
        Instance instance = attachedChunk.getInstance();

        minHeight = instance.getDimensionType().getMinY() - 1;
    }

    protected abstract boolean isBreakBlock(@NotNull Block block);
    public abstract String NBTName();

    public void refresh(int x, int y, int z, Block block) {
        if (isBreakBlock(block)) {
            if (getHeightY(x, z) < y) {
                setHeightY(x, z, y);
            }
        } else if (y == getHeightY(x, z)) {
            refresh(x, z, y - 1);
        }
    }

    public void refresh(int x, int z, int startY) {
        int y = startY;
        while (y > minHeight) {
            Block block = attachedChunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (isBreakBlock(block)) break;
            y--;
        }
        setHeightY(x, z, y);
    }

    public NBTLongArray getNBT(int bitsForHeight) {
        return NBT.LongArray(encode(heights, bitsForHeight));
    }

    public void loadFrom(ImmutableLongArray data, int bitsPerEntry) {
        final int entriesPerLong = 64 / bitsPerEntry;

        final int maxPossibleIndexInContainer = entriesPerLong - 1;
        final int entryMask = (1 << bitsPerEntry) - 1;

        int containerIndex = 0;
        for (int i = 0; i < heights.length; i++) {
            final int indexInContainer = i % entriesPerLong;

            heights[i] = (int)(data.get(containerIndex) >> (indexInContainer * bitsPerEntry)) & entryMask;

            if (indexInContainer == maxPossibleIndexInContainer) containerIndex++;
        }
    }

    // highest breaking block in section
    private int getHeightY(int x, int z) {
        return heights[z << 4 | x] + minHeight;
    }

    private void setHeightY(int x, int z, int height) {
        heights[z << 4 | x] = height - minHeight;
    }

    public static int getStartY(Chunk chunk) {
        int y = chunk.getInstance().getDimensionType().getMaxY();

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
