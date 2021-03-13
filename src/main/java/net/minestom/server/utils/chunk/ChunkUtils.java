package net.minestom.server.utils.chunk;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.callback.OptionalCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class ChunkUtils {

    private ChunkUtils() {

    }

    /**
     * Executes {@link Instance#loadOptionalChunk(int, int, ChunkCallback)} for the array of chunks {@code chunks}
     * with multiple callbacks, {@code eachCallback} which is executed each time a new chunk is loaded and
     * {@code endCallback} when all the chunks in the array have been loaded.
     * <p>
     * Be aware that {@link Instance#loadOptionalChunk(int, int, ChunkCallback)} can give a null chunk in the callback
     * if {@link Instance#hasEnabledAutoChunkLoad()} returns false and the chunk is not already loaded.
     *
     * @param instance     the instance to load the chunks from
     * @param chunks       the chunks to loaded, long value from {@link #getChunkIndex(int, int)}
     * @param eachCallback the optional callback when a chunk get loaded
     * @param endCallback  the optional callback when all the chunks have been loaded
     */
    public static void optionalLoadAll(@NotNull Instance instance, @NotNull long[] chunks,
                                       @Nullable ChunkCallback eachCallback, @Nullable ChunkCallback endCallback) {
        final int length = chunks.length;
        AtomicInteger counter = new AtomicInteger(0);

        for (long visibleChunk : chunks) {
            final int chunkX = ChunkUtils.getChunkCoordX(visibleChunk);
            final int chunkZ = ChunkUtils.getChunkCoordZ(visibleChunk);

            final ChunkCallback callback = (chunk) -> {
                OptionalCallback.execute(eachCallback, chunk);
                final boolean isLast = counter.get() == length - 1;
                if (isLast) {
                    // This is the last chunk to be loaded , spawn player
                    OptionalCallback.execute(endCallback, chunk);
                } else {
                    // Increment the counter of current loaded chunks
                    counter.incrementAndGet();
                }
            };

            // WARNING: if auto load is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(chunkX, chunkZ, callback);
        }
    }

    /**
     * Gets if a chunk is loaded.
     *
     * @param chunk the chunk to check
     * @return true if the chunk is loaded, false otherwise
     */
    public static boolean isLoaded(@Nullable Chunk chunk) {
        return chunk != null && chunk.isLoaded();
    }

    /**
     * Gets if a chunk is loaded.
     *
     * @param instance the instance to check
     * @param x        instance X coordinate
     * @param z        instance Z coordinate
     * @return true if the chunk is loaded, false otherwise
     */
    public static boolean isLoaded(@NotNull Instance instance, double x, double z) {
        final int chunkX = getChunkCoordinate(x);
        final int chunkZ = getChunkCoordinate(z);

        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        return isLoaded(chunk);
    }

    /**
     * @param xz the instance coordinate to convert
     * @return the chunk X or Z based on the argument
     */
    public static int getChunkCoordinate(double xz) {
        final int coordinate = (int) Math.floor(xz);
        assert Chunk.CHUNK_SIZE_X == Chunk.CHUNK_SIZE_Z;
        return Math.floorDiv(coordinate, Chunk.CHUNK_SIZE_X);
    }

    /**
     * Gets the chunk index of chunk coordinates.
     * <p>
     * Used when you want to store a chunk somewhere without using a reference to the whole object
     * (as this can lead to memory leaks).
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a number storing the chunk X and Z
     */
    public static long getChunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static long getChunkIndexWithSection(int chunkX, int chunkZ, int section) {
        long l = 0L;
        l |= ((long) chunkX & 4194303L) << 42;
        l |= ((long) section & 1048575L);
        l |= ((long) chunkZ & 4194303L) << 20;
        return l;
    }

    /**
     * Converts a chunk index to its chunk X position.
     *
     * @param index the chunk index computed by {@link #getChunkIndex(int, int)}
     * @return the chunk X based on the index
     */
    public static int getChunkCoordX(long index) {
        return (int) (index >> 32);
    }

    /**
     * Converts a chunk index to its chunk Z position.
     *
     * @param index the chunk index computed by {@link #getChunkIndex(int, int)}
     * @return the chunk Z based on the index
     */
    public static int getChunkCoordZ(long index) {
        return (int) index;
    }

    public static int getSectionAt(int y) {
        return y / Chunk.CHUNK_SECTION_SIZE;
    }

    /**
     * Gets the chunks in range of a position.
     *
     * @param position the initial position
     * @param range    how far should it retrieves chunk
     * @return an array containing chunks index
     */
    @NotNull
    public static long[] getChunksInRange(@NotNull Position position, int range) {
        range = range * 2;
        long[] visibleChunks = new long[MathUtils.square(range + 1)];
        final int startLoop = -(range / 2);
        final int endLoop = range / 2 + 1;
        int counter = 0;
        for (int x = startLoop; x < endLoop; x++) {
            for (int z = startLoop; z < endLoop; z++) {
                final int chunkX = getChunkCoordinate(position.getX() + Chunk.CHUNK_SIZE_X * x);
                final int chunkZ = getChunkCoordinate(position.getZ() + Chunk.CHUNK_SIZE_Z * z);
                visibleChunks[counter++] = getChunkIndex(chunkX, chunkZ);
            }
        }
        return visibleChunks;
    }

    /**
     * Gets all the loaded neighbours of a chunk and itself, no diagonals.
     *
     * @param instance the instance of the chunks
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @return an array containing all the loaded neighbours chunk index
     */
    @NotNull
    public static long[] getNeighbours(@NotNull Instance instance, int chunkX, int chunkZ) {
        LongList chunks = new LongArrayList();
        // Constants used to loop through the neighbors
        final int[] posX = {1, 0, -1};
        final int[] posZ = {1, 0, -1};

        for (int x : posX) {
            for (int z : posZ) {

                // No diagonal check
                if ((Math.abs(x) + Math.abs(z)) == 2)
                    continue;

                final int targetX = chunkX + x;
                final int targetZ = chunkZ + z;
                final Chunk chunk = instance.getChunk(targetX, targetZ);
                if (ChunkUtils.isLoaded(chunk)) {
                    // Chunk is loaded, add it
                    final long index = getChunkIndex(targetX, targetZ);
                    chunks.add(index);
                }

            }
        }
        return chunks.toArray(new long[0]);
    }

    /**
     * Gets the block index of a position.
     * <p>
     * This can be cast as a short as long as you don't mind receiving a negative value (not array-friendly).
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static int getBlockIndex(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        z = z % Chunk.CHUNK_SIZE_Z;

        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return index & 0xffff;
    }

    /**
     * @param index  an index computed from {@link #getBlockIndex(int, int, int)}
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the instance position of the block located in {@code index}
     */
    @NotNull
    public static BlockPosition getBlockPosition(int index, int chunkX, int chunkZ) {
        final int x = blockIndexToPositionX(index, chunkX);
        final int y = blockIndexToPositionY(index);
        final int z = blockIndexToPositionZ(index, chunkZ);
        return new BlockPosition(x, y, z);
    }

    /**
     * Converts a block chunk index to its instance position X.
     *
     * @param index  the block chunk index from {@link #getBlockIndex(int, int, int)}
     * @param chunkX the chunk X
     * @return the X coordinate of the block index
     */
    public static int blockIndexToPositionX(int index, int chunkX) {
        return (int) blockIndexToChunkPositionX(index) + Chunk.CHUNK_SIZE_X * chunkX;
    }

    /**
     * Converts a block chunk index to its instance position Y.
     *
     * @param index the block chunk index from {@link #getBlockIndex(int, int, int)}
     * @return the Y coordinate of the block index
     */
    public static int blockIndexToPositionY(int index) {
        return (index >>> 4 & 0xFF);
    }

    /**
     * Converts a block chunk index to its instance position Z.
     *
     * @param index  the block chunk index from {@link #getBlockIndex(int, int, int)}
     * @param chunkZ the chunk Z
     * @return the Z coordinate of the block index
     */
    public static int blockIndexToPositionZ(int index, int chunkZ) {
        return (int) blockIndexToChunkPositionZ(index) + Chunk.CHUNK_SIZE_Z * chunkZ;
    }

    /**
     * Converts a block index to a chunk position X.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position X (O-15) of the specified index
     */
    public static byte blockIndexToChunkPositionX(int index) {
        return (byte) (index & 0xF);
    }

    /**
     * Converts a block index to a chunk position Y.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position Y (O-255) of the specified index
     */
    public static short blockIndexToChunkPositionY(int index) {
        return (short) (index >>> 4 & 0xFF);
    }

    /**
     * Converts a block index to a chunk position Z.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position Z (O-15) of the specified index
     */
    public static byte blockIndexToChunkPositionZ(int index) {
        return (byte) (index >> 12 & 0xF);
    }

}
