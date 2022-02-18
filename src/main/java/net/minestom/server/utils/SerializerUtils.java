package net.minestom.server.utils;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public final class SerializerUtils {

    private SerializerUtils() {

    }

    public static long positionToLong(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
    }

    public static Point longToBlockPosition(long value) {
        final int x = (int) (value >> 38);
        final int y = (int) (value << 52 >> 52);
        final int z = (int) (value << 26 >> 38);
        return new Vec(x, y, z);
    }
}
