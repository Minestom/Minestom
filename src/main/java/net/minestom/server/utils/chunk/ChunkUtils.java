package net.minestom.server.utils.chunk;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
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
    public static CompletableFuture<Void> optionalLoadAll(Instance instance, long [] chunks,
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
    public static boolean isLoaded(Instance instance, double x, double z) {
        final Chunk chunk = instance.getChunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z));
        return isLoaded(chunk);
    }

    public static boolean isLoaded(Instance instance, Point point) {
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

    /**
     * Computes the effective view distance based on the client's view distance and the target instance's view distance.
     * The effective view distance is the minimum of the client's view distance and the instance's view distance, plus one.
     *
     * <p>Examples:
     * <ul>
     *   <li>Client VD = 10, Instance VD = 16 → min(10, 16) + 1 = 11</li>
     *   <li>Client VD = 16, Instance VD = 8 → min(16, 8) + 1 = 9</li>
     *   <li>Client VD = 12, Instance = null → min(12, ServerFlag.CHUNK_VIEW_DISTANCE) + 1</li>
     * </ul>
     *
     * @param clientViewDistance the client's view distance setting
     * @param targetInstance the target instance, or null to use the default server view distance
     * @return the effective view distance
     */
    public static int computeEffectiveViewDistance(byte clientViewDistance, @Nullable Instance targetInstance) {
        int maxViewDistance = targetInstance != null ? targetInstance.viewDistance() : ServerFlag.CHUNK_VIEW_DISTANCE;
        return Math.min(clientViewDistance, maxViewDistance) + 1;
    }

    /**
     * Computes the server view distance for the given instance, clamped between 2 and 32.
     *
     * <p>Examples:
     * <ul>
     *   <li>Instance VD = 10 → 10 (within bounds)</li>
     *   <li>Instance VD = 1 → 2 (clamped to minimum)</li>
     *   <li>Instance VD = 50 → 32 (clamped to maximum)</li>
     *   <li>Instance = null → clamp(ServerFlag.CHUNK_VIEW_DISTANCE, 2, 32)</li>
     * </ul>
     *
     * @param targetInstance the target instance, or null to use the default server view distance
     * @return the server view distance, clamped between 2 and 32
     */
    public static int computeServerViewDistance(@Nullable Instance targetInstance) {
        int viewDistance = targetInstance != null ? targetInstance.viewDistance() : ServerFlag.CHUNK_VIEW_DISTANCE;
        return MathUtils.clamp(viewDistance, 2, 32);
    }
}
