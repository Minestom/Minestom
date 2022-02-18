package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * A Batch used when all of the block changed are contained inside a single chunk.
 * If more than one chunk is needed, use an {@link AbsoluteBlockBatch} instead.
 * <p>
 * The batch can be placed in any chunk in any instance, however it will always remain
 * aligned to a chunk border. If completely translatable block changes are needed, use a
 * {@link RelativeBlockBatch} instead.
 * <p>
 * Coordinates are relative to the chunk (0-15) instead of world coordinates.
 *
 * @see Batch
 */
public class ChunkBatch implements Batch<ChunkCallback> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBatch.class);

    private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();
    // Available for other implementations to handle.
    protected final CountDownLatch readyLatch;
    private final BatchOption options;

    public ChunkBatch() {
        this(new BatchOption());
    }

    public ChunkBatch(BatchOption options) {
        this(options, true);
    }

    private ChunkBatch(BatchOption options, boolean ready) {
        this.readyLatch = new CountDownLatch(ready ? 0 : 1);
        this.options = options;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        synchronized (blocks) {
            this.blocks.put(index, block);
        }
    }

    @Override
    public void clear() {
        synchronized (blocks) {
            this.blocks.clear();
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
     * Apply this batch to chunk (0, 0).
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public ChunkBatch apply(@NotNull Instance instance, @Nullable ChunkCallback callback) {
        return apply(instance, 0, 0, callback);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunkX   The x chunk coordinate of the target chunk
     * @param chunkZ   The z chunk coordinate of the target chunk
     * @param callback The callback to be executed when the batch is applied.
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public ChunkBatch apply(@NotNull Instance instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.",
                    chunkX, chunkZ, instance.getUniqueId());
            return null;
        }
        return apply(instance, chunk, callback);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunk    The target chunk
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public ChunkBatch apply(@NotNull Instance instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback) {
        return apply(instance, chunk, callback, true);
    }

    /**
     * Apply this batch to the given chunk, and execute the callback
     * immediately when the blocks have been applied, in an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunk    The target chunk
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public ChunkBatch unsafeApply(@NotNull Instance instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback) {
        return apply(instance, chunk, callback, false);
    }

    /**
     * Apply this batch to the given chunk, and execute the callback depending on safeCallback.
     *
     * @param instance     The instance in which the batch should be applied
     * @param chunk        The target chunk
     * @param callback     The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update.
     *                     Otherwise it will be executed immediately upon completion
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    protected ChunkBatch apply(@NotNull Instance instance,
                               @NotNull Chunk chunk, @Nullable ChunkCallback callback,
                               boolean safeCallback) {
        if (!this.options.isUnsafeApply()) this.awaitReady();

        final ChunkBatch inverse = this.options.shouldCalculateInverse() ? new ChunkBatch(options, false) : null;
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(instance, chunk, inverse, callback, safeCallback));
        return inverse;
    }

    /**
     * Applies this batch in the current thread, executing the callback upon completion.
     */
    private void singleThreadFlush(Instance instance, Chunk chunk, @Nullable ChunkBatch inverse,
                                   @Nullable ChunkCallback callback, boolean safeCallback) {
        try {
            if (!chunk.isLoaded()) {
                LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.",
                        chunk.getChunkX(), chunk.getChunkZ(), instance.getUniqueId());
                return;
            }

            if (this.options.isFullChunk()) {
                // Clear the chunk
                chunk.reset();
            }

            if (blocks.isEmpty()) {
                // Nothing to flush
                OptionalCallback.execute(callback, chunk);
                return;
            }

            final IntSet sections = new IntArraySet();
            synchronized (blocks) {
                for (var entry : blocks.int2ObjectEntrySet()) {
                    final int position = entry.getIntKey();
                    final Block block = entry.getValue();
                    final int section = apply(chunk, position, block, inverse);
                    sections.add(section);
                }
            }

            if (inverse != null) inverse.readyLatch.countDown();
            updateChunk(instance, chunk, sections, callback, safeCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Applies a single block change given a chunk and a value in the described format.
     *
     * @param chunk The chunk to apply the change
     * @param index the block position computed using {@link ChunkUtils#getBlockIndex(int, int, int)}
     * @param block the block to place
     * @return The chunk section which the block was placed
     */
    private int apply(@NotNull Chunk chunk, int index, Block block, @Nullable ChunkBatch inverse) {
        final int x = ChunkUtils.blockIndexToChunkPositionX(index);
        final int y = ChunkUtils.blockIndexToChunkPositionY(index);
        final int z = ChunkUtils.blockIndexToChunkPositionZ(index);
        if (inverse != null) {
            Block prevBlock = chunk.getBlock(x, y, z);
            inverse.setBlock(x, y, z, prevBlock);
        }
        chunk.setBlock(x, y, z, block);
        return ChunkUtils.getChunkCoordinate(y);
    }

    /**
     * Updates the given chunk for all of its viewers, and executes the callback.
     */
    private void updateChunk(@NotNull Instance instance, Chunk chunk, IntSet updatedSections, @Nullable ChunkCallback callback, boolean safeCallback) {
        // Refresh chunk for viewers
        if (options.shouldSendUpdate()) {
            // TODO update all sections from `updatedSections`
            chunk.sendChunk();
        }

        if (instance instanceof InstanceContainer) {
            // FIXME: put method in Instance instead
            ((InstanceContainer) instance).refreshLastBlockChangeTime();
        }

        if (callback != null) {
            if (safeCallback) {
                instance.scheduleNextTick(inst -> callback.accept(chunk));
            } else {
                callback.accept(chunk);
            }
        }
    }
}