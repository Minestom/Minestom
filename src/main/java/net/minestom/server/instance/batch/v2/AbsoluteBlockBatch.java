package net.minestom.server.instance.batch.v2;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Batch which can be used when changes are required across chunk borders,
 * but the changes do not need any translation. If translation is required,
 * use a {@link RelativeBlockBatch} instead.
 * <p>
 * Coordinates are relative to the world origin.
 *
 * @see Batch
 * @see RelativeBlockBatch
 */
public class AbsoluteBlockBatch implements Batch<Runnable> {

    // In the form of <Chunk Index, Batch>
    private final Map<Long, ChunkBatch> data = new HashMap<>();

    public AbsoluteBlockBatch() {}

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        final int chunkX = ChunkUtils.getChunkCoordinate(x);
        final int chunkZ = ChunkUtils.getChunkCoordinate(z);
        final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);

        final ChunkBatch chunkBatch = this.data.computeIfAbsent(chunkIndex, i -> new ChunkBatch());

        final int relativeX = x - (chunkX * Chunk.CHUNK_SIZE_X);
        final int relativeZ = z - (chunkZ * Chunk.CHUNK_SIZE_Z);
        chunkBatch.setSeparateBlocks(relativeX, y, relativeZ, blockStateId, customBlockId, data);
    }

    @Override
    public void clear() {
        synchronized (data) {
            this.data.clear();
        }
    }

    /**
     * Apply this batch to the given instance.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     */
    @Override
    public void apply(@NotNull InstanceContainer instance, @Nullable Runnable callback) {
        apply(instance, callback, true);
    }

    /**
     * Apply this batch to the given instance, and execute the callback immediately when the
     * blocks have been applied, in an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     */
    public void unsafeApply(@NotNull InstanceContainer instance, @Nullable Runnable callback) {
        apply(instance, callback, false);
    }

    /**
     * Apply this batch to the given instance, and execute the callback depending on safeCallback.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update. Otherwise it will be executed immediately upon completion
     *
     */
    protected void apply(@NotNull InstanceContainer instance, @Nullable Runnable callback, boolean safeCallback) {
        synchronized (data) {
            final AtomicInteger counter = new AtomicInteger();
            for (Map.Entry<Long, ChunkBatch> entry : data.entrySet()) {
                final long chunkIndex = entry.getKey();
                final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
                final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
                final ChunkBatch batch = entry.getValue();

                batch.apply(instance, chunkX, chunkZ, c -> {
                    final boolean isLast = counter.incrementAndGet() == data.size();

                    // Execute the callback if this was the last chunk to process
                    if (isLast) {
                        instance.refreshLastBlockChangeTime();
                        if (callback != null) {
                            if (safeCallback)
                                instance.scheduleNextTick(inst -> callback.run());
                            else callback.run();
                        }
                    }
                });
            }
        }
    }
}
