package fr.themode.minestom.utils;

import fr.themode.minestom.Main;

public class ChunkUtils {

    public static long getChunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static int[] getChunkCoord(long index) {
        int chunkX = (int) (index >> 32);
        int chunkZ = (int) index;
        return new int[]{chunkX, chunkZ};
    }

    public static long[] getVisibleChunks(final Position position) {
        final int viewDistance = Main.CHUNK_VIEW_DISTANCE;

        long[] visibleChunks = new long[MathUtils.square(viewDistance + 1)];
        final int startLoop = -(viewDistance / 2);
        final int endLoop = viewDistance / 2 + 1;
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
