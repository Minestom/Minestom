package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public final class CoordConversion {
    public static final int SECTION_BLOCK_COUNT = 16 * 16 * 16;

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

    public static boolean sectionAligned(int xyz) {
        return globalToSectionRelative(xyz) == 0;
    }

    public static boolean sectionAligned(int x, int y, int z) {
        return sectionAligned(x) && sectionAligned(y) && sectionAligned(z);
    }

    public static boolean sectionAligned(Point point) {
        return sectionAligned(point.blockX(), point.blockY(), point.blockZ());
    }

    public static boolean sectionAligned(Point p1, Point p2) {
        final int minX = Math.min(p1.blockX(), p2.blockX());
        final int minY = Math.min(p1.blockY(), p2.blockY());
        final int minZ = Math.min(p1.blockZ(), p2.blockZ());
        final int maxX = Math.max(p1.blockX(), p2.blockX());
        final int maxY = Math.max(p1.blockY(), p2.blockY());
        final int maxZ = Math.max(p1.blockZ(), p2.blockZ());
        return ((minX | minY | minZ) & 0xF) == 0 &&
                ((maxX | maxY | maxZ) & 0xF) == 0xF;
    }

    public static int chunkToRegion(int chunkCoordinate) {
        return chunkCoordinate >> 5;
    }

    public static int chunkToRegionLocal(int chunkCoordinate) {
        return chunkCoordinate & 0x1F;
    }

    public static int floorSection(int coordinate) {
        return coordinate & ~0xF;
    }

    public static int ceilSection(int coordinate) {
        return (coordinate + 15) & ~15;
    }

    // CHUNK INDEX

    public static long chunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static long chunkIndex(Point point) {
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

    public static Point chunkBlockIndexGetGlobal(int index, int chunkX, int chunkZ) {
        final int x = chunkBlockIndexGetX(index) + 16 * chunkX;
        final int y = chunkBlockIndexGetY(index);
        final int z = chunkBlockIndexGetZ(index) + 16 * chunkZ;
        return new Vec(x, y, z);
    }

    public static Point chunkBlockRelativeGetGlobal(int sectionRelativeX, int y, int sectionRelativeZ, int chunkX, int chunkZ) {
        final int x = sectionRelativeX + 16 * chunkX;
        final int z = sectionRelativeZ + 16 * chunkZ;
        return new Vec(x, y, z);
    }

    // SECTION INDEX

    public static long sectionIndex(int sectionX, int sectionY, int sectionZ) {
        // Use 21 bits for each, with sign extension
        final long x = sectionX & 0x1FFFFF;
        final long y = sectionY & 0x1FFFFF;
        final long z = sectionZ & 0x1FFFFF;
        return (x << 42) | (y << 21) | z;
    }

    public static int sectionIndexGetX(long index) {
        int x = (int) (index >> 42) & 0x1FFFFF;
        // Sign extension for 21 bits
        if ((x & 0x100000) != 0) x |= ~0x1FFFFF;
        return x;
    }

    public static int sectionIndexGetY(long index) {
        int y = (int) (index >> 21) & 0x1FFFFF;
        if ((y & 0x100000) != 0) y |= ~0x1FFFFF;
        return y;
    }

    public static int sectionIndexGetZ(long index) {
        int z = (int) index & 0x1FFFFF;
        if ((z & 0x100000) != 0) z |= ~0x1FFFFF;
        return z;
    }

    public static long sectionIndexGlobal(int x, int y, int z) {
        final int sectionX = globalToChunk(x);
        final int sectionY = globalToChunk(y);
        final int sectionZ = globalToChunk(z);
        return sectionIndex(sectionX, sectionY, sectionZ);
    }

    // BLOCK INDEX FROM SECTION (0-15 for each coordinate)

    public static int sectionBlockIndex(int x, int y, int z) {
        return (x << 8) | (z << 4) | y;
    }

    public static int sectionBlockIndexGetX(int index) {
        return (index >> 8) & 0xF;
    }

    public static int sectionBlockIndexGetY(int index) {
        return index & 0xF;
    }

    public static int sectionBlockIndexGetZ(int index) {
        return (index >> 4) & 0xF;
    }

    public static long encodeSectionBlockChange(int sectionBlockIndex, long value) {
        // To use with `MultiBlockChangePacket`
        final long blockState = value << 12;
        return blockState | (long) sectionBlockIndex;
    }

    public static long encodeSectionBlockChange(int localX, int localY, int localZ, long value) {
        return encodeSectionBlockChange(sectionBlockIndex(localX, localY, localZ), value);
    }

    // HASHING

    private static final long PRIME_X = 0x9E37_79B9_7F4A_7C15L;  // Large prime for X axis
    private static final long PRIME_Y = 0xF9F9_F9F9_ECEC_ECECL;  // Large prime for Y axis
    private static final long PRIME_Z = 0xA5A5_A5A5_A5A5_A5A5L;  // Large prime for Z axis

    // Rotation amounts optimized for 3D coordinate separation
    private static final int ROT_X = 31;  // Prime rotation for X
    private static final int ROT_Y = 37;  // Prime rotation for Y
    private static final int ROT_Z = 41;  // Prime rotation for Z

    private static final long AVALANCHE_1 = 0xFF51_AFD7_ED55_8CCDL;
    private static final long AVALANCHE_2 = 0xC4CE_B9FE_1A85_EC53L;

    private static final long INT_SEED = 0xCAFE_BABE_DEAD_BEEFL;
    private static final long DOUBLE_SEED = 0xDEAD_BEEF_CAFE_BABEL;

    public static long hashBlockCoord(int x, int y, int z) {
        long h = INT_SEED;
        h ^= Long.rotateLeft(Integer.toUnsignedLong(x) * PRIME_X, ROT_X);
        h ^= Long.rotateLeft(Integer.toUnsignedLong(y) * PRIME_Y, ROT_Y);
        h ^= Long.rotateLeft(Integer.toUnsignedLong(z) * PRIME_Z, ROT_Z);
        h = Long.rotateLeft(h, 23) ^ (h >>> 17);
        h ^= h >>> 33;
        h *= AVALANCHE_1;
        h ^= h >>> 33;
        h *= AVALANCHE_2;
        h ^= h >>> 33;
        return h;
    }

    public static long hashBlockCoord(Point point) {
        return hashBlockCoord(point.blockX(), point.blockY(), point.blockZ());
    }

    public static long hashGlobalCoord(double x, double y, double z) {
        long h = DOUBLE_SEED;
        long ix = Double.doubleToLongBits(x);
        long iy = Double.doubleToLongBits(y);
        long iz = Double.doubleToLongBits(z);
        long ex = (ix >>> 52) & 0x7FFL;
        long ey = (iy >>> 52) & 0x7FFL;
        long ez = (iz >>> 52) & 0x7FFL;
        long mx = ix & 0x000F_FFFF_FFFF_FFFFL;
        long my = iy & 0x000F_FFFF_FFFF_FFFFL;
        long mz = iz & 0x000F_FFFF_FFFF_FFFFL;
        h ^= Long.rotateLeft(ix * PRIME_X, ROT_X);
        h ^= Long.rotateLeft(iy * PRIME_Y, ROT_Y);
        h ^= Long.rotateLeft(iz * PRIME_Z, ROT_Z);
        h ^= Long.rotateLeft((ex << 32) | (ey << 16) | ez, 19);
        h ^= Long.rotateLeft(mx ^ my ^ mz, 43);
        h = Long.rotateLeft(h, 29) ^ (h >>> 13);
        h ^= h >>> 33;
        h *= AVALANCHE_1;
        h ^= h >>> 33;
        h *= AVALANCHE_2;
        h ^= h >>> 33;
        return h;
    }

    public static long hashGlobalCoord(Point point) {
        return hashGlobalCoord(point.x(), point.y(), point.z());
    }
}
