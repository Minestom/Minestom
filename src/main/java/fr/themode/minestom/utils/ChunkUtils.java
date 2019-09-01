package fr.themode.minestom.utils;

import fr.themode.minestom.instance.Instance;

public class ChunkUtils {

    public static boolean isChunkUnloaded(Instance instance, float x, float z) {
        return instance.getChunk((int) Math.floor(x / 16), (int) Math.floor(z / 16)) == null;
    }

    public static long getChunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static int[] getChunkCoord(long index) {
        int chunkX = (int) (index >> 32);
        int chunkZ = (int) index;
        return new int[]{chunkX, chunkZ};
    }

    public static int getSectionAt(int y) {
        return y / 16;
    }

    public static long[] getChunksInRange(final Position position, int range) {

        long[] visibleChunks = new long[MathUtils.square(range + 1)];
        final int startLoop = -(range / 2);
        final int endLoop = range / 2 + 1;
        int counter = 0;
        for (int x = startLoop; x < endLoop; x++) {
            for (int z = startLoop; z < endLoop; z++) {
                int chunkX = Math.floorDiv((int) (position.getX() + 16 * x), 16);
                int chunkZ = Math.floorDiv((int) (position.getZ() + 16 * z), 16);
                visibleChunks[counter] = getChunkIndex(chunkX, chunkZ);
                counter++;
            }
        }
        return visibleChunks;
    }

}
