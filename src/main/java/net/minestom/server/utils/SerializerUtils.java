package net.minestom.server.utils;

public class SerializerUtils {

    public static byte[] intToBytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) (value >> 24);
        result[1] = (byte) (value >> 16);
        result[2] = (byte) (value >> 8);
        result[3] = (byte) (value >> 0);
        return result;
    }

    public static int bytesToInt(byte[] value) {
        return ((value[0] & 0xFF) << 24) |
                ((value[1] & 0xFF) << 16) |
                ((value[2] & 0xFF) << 8) |
                ((value[3] & 0xFF) << 0);
    }

    public static int chunkCoordToIndex(int x, int y, int z) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return index & 0xffff;
    }

    public static byte[] indexToChunkPosition(int index) {
        byte z = (byte) (index >> 12 & 0xF);
        byte y = (byte) (index >> 4 & 0xFF);
        byte x = (byte) (index >> 0 & 0xF);
        return new byte[]{x, y, z};
    }

    public static BlockPosition indexToChunkBlockPosition(int index) {
        byte z = (byte) (index >> 12 & 0xF);
        byte y = (byte) (index >> 4 & 0xFF);
        byte x = (byte) (index >> 0 & 0xF);
        return new BlockPosition(x, y, z);
    }

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
