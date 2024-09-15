package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Batch} which can be used when changes are required across chunk borders,
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
    private final Long2ObjectMap<ChunkBatch> chunkBatchesMap = new Long2ObjectOpenHashMap<>();

    // Available for other implementations to handle.
    protected final CountDownLatch readyLatch;
    private final BatchOption options;

    private volatile BatchOption inverseOption = new BatchOption();

    public AbsoluteBlockBatch() {
        this(new BatchOption());
    }

    public AbsoluteBlockBatch(BatchOption options) {
        this(options, true);
    }

    private AbsoluteBlockBatch(BatchOption options, boolean ready) {
        this.readyLatch = new CountDownLatch(ready ? 0 : 1);
        this.options = options;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int chunkX = CoordConversion.globalToChunk(x);
        final int chunkZ = CoordConversion.globalToChunk(z);
        final long chunkIndex = CoordConversion.chunkIndex(chunkX, chunkZ);

        final ChunkBatch chunkBatch;
        synchronized (chunkBatchesMap) {
            chunkBatch = chunkBatchesMap.computeIfAbsent(chunkIndex, i -> new ChunkBatch(this.options));
        }

        final int relativeX = x - (chunkX * Chunk.CHUNK_SIZE_X);
        final int relativeZ = z - (chunkZ * Chunk.CHUNK_SIZE_Z);
        chunkBatch.setBlock(relativeX, y, relativeZ, block);
    }

    @Override
    public void clear() {
        synchronized (chunkBatchesMap) {
            this.chunkBatchesMap.clear();
        }
    }

    @Override
    public boolean isReady() {
        return this.readyLatch.getCount() == 0;
    }

    @Override
    public void awaitReady() {
        try {
            this.readyLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("#awaitReady interrupted!", e);
        }
    }

    /**
     * Applies this batch to the given instance.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public AbsoluteBlockBatch apply(@NotNull Instance instance, @Nullable Runnable callback) {
        return apply(instance, callback, true);
    }

    /**
     * Applies this batch to the given instance, and execute the callback immediately when the
     * blocks have been applied, in an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public AbsoluteBlockBatch unsafeApply(@NotNull Instance instance, @Nullable Runnable callback) {
        return apply(instance, callback, false);
    }

    /**
     * Applies this batch to the given instance, and execute the callback depending on safeCallback.
     *
     * @param instance     The instance in which the batch should be applied
     * @param callback     The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update.
     *                     Otherwise it will be executed immediately upon completion
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    protected AbsoluteBlockBatch apply(@NotNull Instance instance, @Nullable Runnable callback, boolean safeCallback) {
        if (!this.options.isUnsafeApply()) this.awaitReady();

        final AbsoluteBlockBatch inverse = this.options.shouldCalculateInverse() ? new AbsoluteBlockBatch(inverseOption) : null;
        synchronized (chunkBatchesMap) {
            AtomicInteger counter = new AtomicInteger();
            Set<Chunk> updated = ConcurrentHashMap.newKeySet();

            for (var entry : Long2ObjectMaps.fastIterable(chunkBatchesMap)) {
                final long chunkIndex = entry.getLongKey();
                final int chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                final int chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                final ChunkBatch batch = entry.getValue();
                ChunkBatch chunkInverse = batch.apply(instance, chunkX, chunkZ, c -> {
                    final boolean isLast = counter.incrementAndGet() == chunkBatchesMap.size();
                    // Execute the callback if this was the last chunk to process
                    if (isLast) {
                        if (inverse != null) inverse.readyLatch.countDown();
                        if (instance instanceof InstanceContainer) {
                            // FIXME: put method in Instance instead
                            ((InstanceContainer) instance).refreshLastBlockChangeTime();
                        }
                        if (callback != null) {
                            if (safeCallback) {
                                instance.scheduleNextTick(inst -> callback.run());
                            } else {
                                callback.run();
                            }
                        }

                        Set<Chunk> expanded = new HashSet<>();
                        for (Chunk chunk : updated) {
                            for (int i = -1; i <= 1; ++i) {
                                for (int j = -1; j <= 1; ++j) {
                                    Chunk toAdd = instance.getChunk(chunk.getChunkX() + i, chunk.getChunkZ() + j);
                                    if (toAdd != null) {
                                        expanded.add(toAdd);
                                    }
                                }
                            }
                        }

                        // Update the chunk's light
                        for (Chunk chunk : expanded) {
                            if (chunk instanceof LightingChunk dc) {
                                dc.sendLighting();
                            }
                        }
                    }
                });
                if (inverse != null) inverse.chunkBatchesMap.put(chunkIndex, chunkInverse);
            }
        }

        return inverse;
    }

    public @NotNull BatchOption getInverseOption() {
        return inverseOption;
    }

    public void setInverseOption(@NotNull BatchOption inverseOption) {
        this.inverseOption = inverseOption;
    }
}
