package net.minestom.server.utils.chunk;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;

public class ChunkUtils {

    /**
     * @param instance the instance where {@code chunk} is
     * @param chunk    the chunk to check
     * @return true if the chunk is unloaded, false otherwise
     */
    public static boolean isChunkUnloaded(Instance instance, Chunk chunk) {
        return chunk == null || !chunk.isLoaded();
    }

    /**
     * @param instance the instance to check
     * @param x        instance X coordinate
     * @param z        instance Z coordinate
     * @return true if the chunk is unloaded, false otherwise
     */
    public static boolean isChunkUnloaded(Instance instance, float x, float z) {
        int chunkX = getChunkCoordinate((int) x);
        int chunkZ = getChunkCoordinate((int) z);

        Chunk chunk = instance.getChunk(chunkX, chunkZ);
        return isChunkUnloaded(instance, chunk);
    }

    /**
     * @param xz the instance coordinate to convert
     * @return the chunk X or Z based on the argument
     */
    public static int getChunkCoordinate(int xz) {
        // Assume Chunk.CHUNK_SIZE_X == Chunk.CHUNK_SIZE_Z
        return Math.floorDiv(xz, Chunk.CHUNK_SIZE_X);
    }

    /**
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a number storing the chunk X and Z
     */
    public static long getChunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    /**
     * @param index the chunk index computed by {@link #getChunkIndex(int, int)}
     * @return an array containing both the chunk X and Z (index 0 = X; index 1 = Z)
     */
    public static int[] getChunkCoord(long index) {
        int chunkX = (int) (index >> 32);
        int chunkZ = (int) index;
        return new int[]{chunkX, chunkZ};
    }

    public static int getSectionAt(int y) {
        return y / Chunk.CHUNK_SECTION_SIZE;
    }

    /**
     * @param position the initial position
     * @param range    how far should it retrieves chunk
     * @return an array containing chunks index which can be converted using {@link #getChunkCoord(long)}
     */
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

    public static int getBlockIndex(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        z = z % Chunk.CHUNK_SIZE_Z;

        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return index & 0xffff;
    }

    public static BlockPosition getBlockPosition(int index, int chunkX, int chunkZ) {
        int[] pos = indexToPosition(index, chunkX, chunkZ);
        return new BlockPosition(pos[0], pos[1], pos[2]);
    }

    public static int[] indexToPosition(int index, int chunkX, int chunkZ) {
        int z = (byte) (index >> 12 & 0xF);
        int y = (index >>> 4 & 0xFF);
        int x = (byte) (index >> 0 & 0xF);

        x += 16 * chunkX;
        z += 16 * chunkZ;

        return new int[]{x, y, z};
    }

    public static int[] indexToChunkPosition(int index) {
        return indexToPosition(index, 0, 0);
    }

}
