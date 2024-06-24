package net.minestom.server.utils;

public final class CoordConversionUtils {
    public static int globalToChunk(double xz) {
        return globalToChunk((int) Math.floor(xz));
    }

    public static int globalToChunk(int xz) {
        // Assume chunk/section size being 16 (4 bits)
        return xz >> 4;
    }
}
