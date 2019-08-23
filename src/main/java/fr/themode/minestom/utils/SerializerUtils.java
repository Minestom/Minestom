package fr.themode.minestom.utils;

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

    public static BlockPosition indexToBlockPosition(int index, int chunkX, int chunkZ) {
        byte z = (byte) (index >> 12 & 0xF);
        byte y = (byte) (index >> 4 & 0xFF);
        byte x = (byte) (index >> 0 & 0xF);
        return new BlockPosition(x + 16 * chunkX, y, z + 16 * chunkZ);
    }

}
