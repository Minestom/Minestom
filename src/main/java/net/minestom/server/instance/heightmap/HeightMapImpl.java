package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public class HeightMapImpl implements HeightMap {
    private final int[] motionBlocking = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final int[] surface = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final Chunk attachedChunk;
    private final Instance instance;

    private final int minY;
    private final int maxY;

    public HeightMapImpl(Chunk attachedChunk) {
        this.attachedChunk = attachedChunk;
        this.instance = attachedChunk.getInstance();

        minY = instance.getDimensionType().getMinY();
        maxY = minY + instance.getDimensionType().getHeight();
    }

    @Override
    public void refresh() {
        synchronized (attachedChunk) {
            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    refreshAt(x, z);
                }
            }
        }
    }

    @Override
    public void refreshAt(int x, int z) {
        refreshSurface(x, z, maxY, minY);
        refreshMotionBlocking(x, z, maxY, minY);
    }

    private void refreshSurface(int x, int z, int maxY, int minY) {
        int y = maxY;
        while (y >= minY) {
            Block block = attachedChunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (!block.isAir()) break;
            y--;
        }
        surface[z << 4 | x] = (y + 1 - minY);
    }

    private void refreshMotionBlocking(int x, int z, int maxY, int minY) {
        int y = maxY;
        while (y >= minY) {
            Block block = attachedChunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) continue;
            if (isMotionBlocking(block)) break;
            y--;
        }
        motionBlocking[z << 4 | x] = (y + 1 - minY);
    }

    private static boolean isMotionBlocking(@NotNull Block block) {
        return block.isSolid() || block.isLiquid() || "true".equals(block.getProperty("waterlogged"));
    }

    @Override
    public NBTCompound getNBT() {
        refresh();

        final int dimensionHeight = instance.getDimensionType().getHeight();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return NBT.Compound(Map.of(
                "MOTION_BLOCKING", NBT.LongArray(encodeHeightMap(motionBlocking, bitsForHeight)),
                "WORLD_SURFACE", NBT.LongArray(encodeHeightMap(surface, bitsForHeight))));
    }

    /**
     * Creates compressed longs array from uncompressed heights array.
     *
     * @param heights array of heights. Note that for this method it doesn't matter what size this array will be.
     * But to get correct heights, array must be 256 elements long, and at index `i` must be height of (z=i/16, x=i%16).
     * @param bitsPerEntry bits that each entry from height will take in `long` container.
     * @return array of encoded heights.
     */
    private static long[] encodeHeightMap(int[] heights, int bitsPerEntry) {
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
