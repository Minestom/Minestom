package net.minestom.server.utils;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

public class ChunkUtils {

    public static boolean isChunkUnloaded(Instance instance, float x, float z) {
        int chunkX = getChunkCoordinate((int) x);
        int chunkZ = getChunkCoordinate((int) z);

        Chunk chunk = instance.getChunk(chunkX, chunkZ);
        return chunk == null || !chunk.isLoaded();
    }

    public static int getChunkCoordinate(int xz) {
        // Assume Chunk.CHUNK_SIZE_X == Chunk.CHUNK_SIZE_Z
        return Math.floorDiv(xz, Chunk.CHUNK_SIZE_X);
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
        return y / Chunk.CHUNK_SECTION_SIZE;
    }

    public static long[] getChunksInRange(final Position position, int range) {
        long[] visibleChunks = new long[MathUtils.square(range + 1)];
        final int startLoop = -(range / 2);
        final int endLoop = range / 2 + 1;
        int counter = 0;
        for (int x = startLoop; x < endLoop; x++) {
            for (int z = startLoop; z < endLoop; z++) {
                int chunkX = getChunkCoordinate((int) (position.getX() + Chunk.CHUNK_SIZE_X * x));
                int chunkZ = getChunkCoordinate((int) (position.getZ() + Chunk.CHUNK_SIZE_Z * z));
                visibleChunks[counter] = getChunkIndex(chunkX, chunkZ);
                counter++;
            }
        }
        return visibleChunks;
    }

}
