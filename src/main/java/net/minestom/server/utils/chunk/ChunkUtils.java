package net.minestom.server.utils.chunk;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.function.IntegerBiConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public final class ChunkUtils {

    private ChunkUtils() {
    }

    /**
     * Executes {@link Instance#loadOptionalChunk(int, int)} for the array of chunks {@code chunks}
     * with multiple callbacks, {@code eachCallback} which is executed each time a new chunk is loaded and
     * {@code endCallback} when all the chunks in the array have been loaded.
     * <p>
     * Be aware that {@link Instance#loadOptionalChunk(int, int)} can give a null chunk in the callback
     * if {@link Instance#hasEnabledAutoChunkLoad()} returns false and the chunk is not already loaded.
     *
     * @param instance     the instance to load the chunks from
     * @param chunks       the chunks to loaded, long value from {@link #getChunkIndex(int, int)}
     * @param eachCallback the optional callback when a chunk get loaded
     * @return a {@link CompletableFuture} completed once all chunks have been processed
     */
    public static @NotNull CompletableFuture<Void> optionalLoadAll(@NotNull Instance instance, long @NotNull [] chunks,
                                                                   @Nullable ChunkCallback eachCallback) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        AtomicInteger counter = new AtomicInteger(0);
        for (long visibleChunk : chunks) {
            // WARNING: if autoload is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(getChunkCoordX(visibleChunk), getChunkCoordZ(visibleChunk))
                    .thenAccept((chunk) -> {
                        OptionalCallback.execute(eachCallback, chunk);
                        final boolean isLast = counter.get() == chunks.length - 1;
                        if (isLast) {
                            // This is the last chunk to be loaded , spawn player
                            completableFuture.complete(null);
                        } else {
                            // Increment the counter of current loaded chunks
                            counter.incrementAndGet();
                        }
                    });
        }
        return completableFuture;
    }

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
        final Chunk chunk = instance.getChunk(getChunkCoordinate(x), getChunkCoordinate(z));
        return isLoaded(chunk);
    }

    public static boolean isLoaded(@NotNull Instance instance, @NotNull Point point) {
        final Chunk chunk = instance.getChunk(point.chunkX(), point.chunkZ());
        return isLoaded(chunk);
    }

    public static Chunk retrieve(Instance instance, Chunk originChunk, double x, double z) {
        final int chunkX = getChunkCoordinate(x);
        final int chunkZ = getChunkCoordinate(z);
        final boolean sameChunk = originChunk.getChunkX() == chunkX &&
                originChunk.getChunkZ() == chunkZ;
        return sameChunk ? originChunk : instance.getChunk(chunkX, chunkZ);
    }

    public static Chunk retrieve(Instance instance, Chunk originChunk, Point position) {
        return retrieve(instance, originChunk, position.x(), position.z());
    }

    /**
     * @param xz the instance coordinate to convert
     * @return the chunk X or Z based on the argument
     */
    public static int getChunkCoordinate(double xz) {
        return getChunkCoordinate((int) Math.floor(xz));
    }

    public static int getChunkCoordinate(int xz) {
        // Assume chunk/section size being 16 (4 bits)
        return xz >> 4;
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

    public static long getChunkIndex(@NotNull Chunk chunk) {
        return getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
    }

    public static long getChunkIndex(@NotNull Point point) {
        return getChunkIndex(point.chunkX(), point.chunkZ());
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

    public static int getChunkCount(int range) {
        if (range < 0) {
            throw new IllegalArgumentException("Range cannot be negative");
        }
        final int square = range * 2 + 1;
        return square * square;
    }

    public static void forDifferingChunksInRange(int newChunkX, int newChunkZ,
                                                 int oldChunkX, int oldChunkZ,
                                                 int range, @NotNull IntegerBiConsumer callback) {
        for (int x = newChunkX - range; x <= newChunkX + range; x++) {
            for (int z = newChunkZ - range; z <= newChunkZ + range; z++) {
                if (Math.abs(x - oldChunkX) > range || Math.abs(z - oldChunkZ) > range) {
                    callback.accept(x, z);
                }
            }
        }
    }

    public static void forDifferingChunksInRange(int newChunkX, int newChunkZ,
                                                 int oldChunkX, int oldChunkZ,
                                                 int range,
                                                 @NotNull IntegerBiConsumer newCallback, @NotNull IntegerBiConsumer oldCallback) {
        // Find the new chunks
        forDifferingChunksInRange(newChunkX, newChunkZ, oldChunkX, oldChunkZ, range, newCallback);
        // Find the old chunks
        forDifferingChunksInRange(oldChunkX, oldChunkZ, newChunkX, newChunkZ, range, oldCallback);
    }

    public static void forChunksInRange(int chunkX, int chunkZ, int range, IntegerBiConsumer consumer) {
        for (int x = -range; x <= range; ++x) {
            for (int z = -range; z <= range; ++z) {
                consumer.accept(chunkX + x, chunkZ + z);
            }
        }
    }

    public static void forChunksInRange(@NotNull Point point, int range, IntegerBiConsumer consumer) {
        forChunksInRange(point.chunkX(), point.chunkZ(), range, consumer);
    }

    /**
     * Gets the block index of a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static int getBlockIndex(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        z = z % Chunk.CHUNK_SIZE_Z;

        int index = x & 0xF; // 4 bits
        index |= (y << 4) & 0x0FFFFFF0; // 24 bits
        index |= (z << 28) & 0xF0000000; // 4 bits
        return index;
    }

    /**
     * @param index  an index computed from {@link #getBlockIndex(int, int, int)}
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the instance position of the block located in {@code index}
     */
    public static @NotNull Point getBlockPosition(int index, int chunkX, int chunkZ) {
        final int x = blockIndexToChunkPositionX(index) + Chunk.CHUNK_SIZE_X * chunkX;
        final int y = index >>> 4 & 0xFF;
        final int z = blockIndexToChunkPositionZ(index) + Chunk.CHUNK_SIZE_Z * chunkZ;
        return new Vec(x, y, z);
    }

    /**
     * Converts a block index to a chunk position X.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position X (O-15) of the specified index
     */
    public static int blockIndexToChunkPositionX(int index) {
        return index & 0xF; // 0-4 bits
    }

    /**
     * Converts a block index to a chunk position Y.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position Y of the specified index
     */
    public static int blockIndexToChunkPositionY(int index) {
        return (index >> 4) & 0x0FFFFFF; // 4-28 bits
    }

    /**
     * Converts a block index to a chunk position Z.
     *
     * @param index an index computed from {@link #getBlockIndex(int, int, int)}
     * @return the chunk position Z (O-15) of the specified index
     */
    public static int blockIndexToChunkPositionZ(int index) {
        return (index >> 28) & 0xF; // 28-32 bits
    }

    /**
     * Converts a global coordinate value to a section coordinate
     *
     * @param xyz global coordinate
     * @return section coordinate
     */
    public static int toSectionRelativeCoordinate(int xyz) {
        xyz %= 16;
        if (xyz < 0) {
            xyz += Chunk.CHUNK_SECTION_SIZE;
        }
        return xyz;
    }
}
