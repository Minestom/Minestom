package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

public final class CoordConversion {
    // COORDINATE CONVERSIONS

    public static int globalToBlock(double xyz) {
        return (int) Math.floor(xyz);
    }

    public static int globalToChunk(double xz) {
        final int block = globalToBlock(xz);
        return globalToChunk(block);
    }

    public static int globalToChunk(int xz) {
        // Assume chunk/section size being 16 (4 bits)
        return xz >> 4;
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

    // CHUNK INDEX

    public static long chunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static long chunkIndex(@NotNull Point point) {
        return chunkIndex(point.chunkX(), point.chunkZ());
    }

    public static int chunkIndexGetX(long index) {
        return (int) (index >> 32);
    }

    public static int chunkIndexGetZ(long index) {
        return (int) index;
    }

    // BLOCK INDEX FROM CHUNK

    public static int chunkBlockIndex(int x, int y, int z) {
        // Mask x and z to ensure only the lower 4 bits are used.
        x = globalToSectionRelative(x);
        z = globalToSectionRelative(z);

        // Bits layout:
        // bits 0-3: x (4 bits)
        // bits 4-26: absolute value of y (23 bits)
        // bit 27: sign bit of y
        // bits 28-31: z (4 bits)
        return (z << 28)                          // Z component (shifted to the upper 4 bits)
                | (y & 0x80000000) >>> 4          // Y sign bit if y was negative
                | (Math.abs(y) & 0x007FFFFF) << 4 // Y component (23 bits for Y, sign encoded in the 24th)
                | (x);                            // X component (4 bits for X)
    }

    public static int chunkBlockIndexGetX(int index) {
        return index & 0xF; // bits 0-3
    }

    public static int chunkBlockIndexGetY(int index) {
        int y = (index & 0x07FFFFF0) >>> 4;
        if ((index & 0x08000000) != 0) y = -y; // Sign bit set, invert sign
        return y; // 4-28 bits
    }

    public static int chunkBlockIndexGetZ(int index) {
        return (index >>> 28) & 0xF; // bits 28-31
    }

    public static @NotNull Point chunkBlockIndexGetGlobal(int index, int chunkX, int chunkZ) {
        final int x = chunkBlockIndexGetX(index) + 16 * chunkX;
        final int y = chunkBlockIndexGetY(index);
        final int z = chunkBlockIndexGetZ(index) + 16 * chunkZ;
        return new Vec(x, y, z);
    }
}
