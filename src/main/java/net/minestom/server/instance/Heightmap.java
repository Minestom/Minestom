package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static net.minestom.server.coordinate.CoordConversion.globalToChunk;
import static net.minestom.server.coordinate.CoordConversion.globalToSectionRelative;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public final class Heightmap {
    public enum Type {
        WORLD_SURFACE_WG,
        WORLD_SURFACE,
        OCEAN_FLOOR_WG,
        OCEAN_FLOOR,
        MOTION_BLOCKING,
        MOTION_BLOCKING_NO_LEAVES;

        public static final NetworkBuffer.Type<Type> NETWORK_TYPE = NetworkBuffer.Enum(Type.class);
    }

    public static Heightmap motionBlocking(Chunk chunk) {
        return new Heightmap(chunk, Type.MOTION_BLOCKING, block ->
                (block.isSolid() && !block.compare(Block.COBWEB) && !block.compare(Block.BAMBOO_SAPLING))
                        || block.isLiquid()
                        || "true".equals(block.getProperty("waterlogged")));
    }

    public static Heightmap worldSurface(Chunk chunk) {
        return new Heightmap(chunk, Type.WORLD_SURFACE, block -> !block.isAir());
    }

    private final short[] heights = new short[CHUNK_SIZE_X * CHUNK_SIZE_Z];
    private final ChunkImpl chunk;
    private final Type type;
    private final Predicate<Block> predicate;
    private final int minHeight;
    private boolean needsRefresh = true;

    public Heightmap(Chunk chunk, Type type, Predicate<Block> predicate) {
        this.chunk = (ChunkImpl) chunk;
        this.type = type;
        this.predicate = predicate;
        this.minHeight = this.chunk.dimension.minY() - 1;
    }

    public @NotNull Type type() {
        return type;
    }

    public void refresh(int x, int y, int z, Block block) {
        final int height = getHeight(x, z);
        if (predicate.test(block)) {
            if (height < y) setHeightY(x, z, y);
        } else if (y == height) {
            refresh(x, z, y - 1);
        }
    }

    public void refresh(int startY) {
        if (!needsRefresh) return;
        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                refresh(x, z, startY);
            }
        }
        needsRefresh = false;
    }

    public void refresh(int x, int z, int startY) {
        final int localX = globalToSectionRelative(x);
        final int localZ = globalToSectionRelative(z);

        int foundHeight = minHeight;
        int currentY = startY;
        while (currentY > minHeight) {
            final int sectionY = globalToChunk(currentY);
            if (sectionY < chunk.getMinSection() || sectionY >= chunk.getMaxSection()) {
                currentY = (sectionY << 4) - 1; // Move to the bottom of the previous section
                continue;
            }

            final Palette blockPalette = chunk.getSection(sectionY).blockPalette();
            final int localHeight = blockPalette.height(localX, localZ, (px, py, pz, value) -> {
                if (value == 0) return false;
                final Block block = Block.fromStateId(value);
                return block != null && predicate.test(block);
            });
            if (localHeight >= 0) {
                // Found a matching block, convert local Y back to world Y
                foundHeight = (sectionY << 4) + localHeight;
                break;
            }

            // No matching block found in this section, move to the section below
            currentY = (sectionY << 4) - 1;
        }
        setHeightY(x, z, foundHeight);
    }

    public long[] getNBT() {
        final int dimensionHeight = chunk.dimension.height();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return encode(heights, bitsForHeight);
    }

    public void loadFrom(long[] data) {
        final int dimensionHeight = chunk.dimension.height();
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
        int y = ((ChunkImpl) chunk).dimension.maxY();
        final int sectionsCount = chunk.getMaxSection() - chunk.getMinSection();
        for (int i = 0; i < sectionsCount; i++) {
            final int sectionY = chunk.getMaxSection() - i - 1;
            final Palette blockPalette = chunk.getSection(sectionY).blockPalette();
            if (!blockPalette.isEmpty()) break;
            y -= 16;
        }
        return y;
    }

    /**
     * Creates compressed longs array from uncompressed heights array.
     *
     * @param heights      array of heights. Note that for this method it doesn't
     *                     matter what size this array will be.
     *                     But to get correct heights, array must be 256 elements
     *                     long, and at index `i` must be height of (z=i/16,
     *                     x=i%16).
     * @param bitsPerEntry bits that each entry from height will take in `long`
     *                     container.
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
