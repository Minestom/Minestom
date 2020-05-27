package net.minestom.server.utils;

public class SerializerUtils {

    public static long positionToLong(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
    }

    public static BlockPosition longToBlockPosition(long value) {
        int x = (int) (value >> 38);
        int y = (int) (value & 0xFFF);
        int z = (int) (value << 26 >> 38);
        return new BlockPosition(x, y, z);
    }

}
