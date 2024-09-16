package net.minestom.server.utils.chunk;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
     * @param chunks       the chunks to loaded, long value from {@link CoordConversion#chunkIndex(int, int)}
     * @param eachCallback the optional callback when a chunk get loaded
     * @return a {@link CompletableFuture} completed once all chunks have been processed
     */
    public static @NotNull CompletableFuture<Void> optionalLoadAll(@NotNull Instance instance, long @NotNull [] chunks,
                                                                   @Nullable Consumer<Chunk> eachCallback) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        AtomicInteger counter = new AtomicInteger(0);
        for (long visibleChunk : chunks) {
            // WARNING: if autoload is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(CoordConversion.chunkIndexGetX(visibleChunk), CoordConversion.chunkIndexGetZ(visibleChunk))
                    .thenAccept((chunk) -> {
                        if (eachCallback != null) eachCallback.accept(chunk);
                        if (counter.incrementAndGet() == chunks.length) {
                            // This is the last chunk to be loaded , spawn player
                            completableFuture.complete(null);
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
        final Chunk chunk = instance.getChunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z));
        return isLoaded(chunk);
    }

    public static boolean isLoaded(@NotNull Instance instance, @NotNull Point point) {
        final Chunk chunk = instance.getChunk(point.chunkX(), point.chunkZ());
        return isLoaded(chunk);
    }

    public static Chunk retrieve(Instance instance, Chunk originChunk, double x, double z) {
        final int chunkX = CoordConversion.globalToChunk(x);
        final int chunkZ = CoordConversion.globalToChunk(z);
        final boolean sameChunk = originChunk != null &&
                originChunk.getChunkX() == chunkX && originChunk.getChunkZ() == chunkZ;
        return sameChunk ? originChunk : instance.getChunk(chunkX, chunkZ);
    }

    public static Chunk retrieve(Instance instance, Chunk originChunk, Point position) {
        return retrieve(instance, originChunk, position.x(), position.z());
    }
}
