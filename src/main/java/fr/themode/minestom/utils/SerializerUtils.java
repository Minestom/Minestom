package fr.themode.minestom.utils;

public class SerializerUtils {

    public static int chunkCoordToIndex(byte x, byte y, byte z) {
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
