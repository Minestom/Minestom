package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

public final class CoordConversionUtils {
    private static final int CHUNK_SIZE_X = 16;
    private static final int CHUNK_SIZE_Z = 16;

    // COORDINATE CONVERSIONS

    public static int globalToChunk(double xz) {
        return globalToChunk((int) Math.floor(xz));
    }

    public static int globalToChunk(int xz) {
        // Assume chunk/section size being 16 (4 bits)
        return xz >> 4;
    }

    public static int globalToSection(int xyz) {
        return xyz & 0xF;
    }


    // INDEX CONVERSIONS

    /**
     * Gets the chunk index of chunk coordinates.
     * <p>
     * Used when you want to store a chunk somewhere without using a reference to the whole object
     * (as this can lead to memory leaks).
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a number storing the chunk X and Z
     */
    public static long chunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static long chunkIndex(@NotNull Point point) {
        return chunkIndex(point.chunkX(), point.chunkZ());
    }

    /**
     * Converts a chunk index to its chunk X position.
     *
     * @param index the chunk index computed by {@link #chunkIndex(int, int)}
     * @return the chunk X based on the index
     */
    public static int chunkIndexToChunkX(long index) {
        return (int) (index >> 32);
    }

    /**
     * Converts a chunk index to its chunk Z position.
     *
     * @param index the chunk index computed by {@link #chunkIndex(int, int)}
     * @return the chunk Z based on the index
     */
    public static int chunkIndexToChunkZ(long index) {
        return (int) index;
    }


    /**
     * Gets the block index of a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static int blockIndex(int x, int y, int z) {
        x = x % CHUNK_SIZE_X;
        z = z % CHUNK_SIZE_Z;

        int index = x & 0xF; // 4 bits
        if (y > 0) {
            index |= (y << 4) & 0x07FFFFF0; // 23 bits (24th bit is always 0 because y is positive)
        } else {
            index |= ((-y) << 4) & 0x7FFFFF0; // Make positive and use 23 bits
            index |= 1 << 27; // Set negative sign at 24th bit
        }
        index |= (z << 28) & 0xF0000000; // 4 bits
        return index;
    }

    /**
     * Converts a block index to a chunk position X.
     *
     * @param index an index computed from {@link #blockIndex(int, int, int)}
     * @return the chunk position X (O-15) of the specified index
     */
    public static int blockIndexToChunkPositionX(int index) {
        return index & 0xF; // 0-4 bits
    }

    /**
     * Converts a block index to a chunk position Y.
     *
     * @param index an index computed from {@link #blockIndex(int, int, int)}
     * @return the chunk position Y of the specified index
     */
    public static int blockIndexToChunkPositionY(int index) {
        int y = (index & 0x07FFFFF0) >>> 4;
        if (((index >>> 27) & 1) == 1) y = -y; // Sign bit set, invert sign
        return y; // 4-28 bits
    }

    /**
     * Converts a block index to a chunk position Z.
     *
     * @param index an index computed from {@link #blockIndex(int, int, int)}
     * @return the chunk position Z (O-15) of the specified index
     */
    public static int blockIndexToChunkPositionZ(int index) {
        return (index >> 28) & 0xF; // 28-32 bits
    }

    /**
     * @param index  an index computed from {@link #blockIndex(int, int, int)}
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the instance position of the block located in {@code index}
     */
    public static @NotNull Point blockIndexToGlobal(int index, int chunkX, int chunkZ) {
        final int x = blockIndexToChunkPositionX(index) + CHUNK_SIZE_X * chunkX;
        final int y = blockIndexToChunkPositionY(index);
        final int z = blockIndexToChunkPositionZ(index) + CHUNK_SIZE_Z * chunkZ;
        return new Vec(x, y, z);
    }

    public static int globalToSectionRelative(int xyz) {
        return xyz & 0xF;
    }

    public static int chunkToRegion(int chunkCoordinate) {
        return chunkCoordinate >> 5;
    }

    public static int chunkToRegionLocal(int chunkCoordinate) {
        return chunkCoordinate & 0x1F;
    }

    public static int floorSection(int coordinate) {
        return coordinate - (coordinate & 0xF);
    }

    public static int ceilSection(int coordinate) {
        return ((coordinate - 1) | 15) + 1;
    }
}
