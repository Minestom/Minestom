package net.minestom.server.instance.batch.v2;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Need to be synchronized manually
    // Format: blockIndex/blockStateId/customBlockId (32/16/16 bits)
    private final LongList blocks;

    // Need to be synchronized manually
    // block index - data
    private final Int2ObjectMap<Data> blockDataMap;

    public ChunkBatch() {
        this(new LongArrayList(), new Int2ObjectOpenHashMap<>());
    }

    protected ChunkBatch(LongList blocks, Int2ObjectMap<Data> blockDataMap) {
        this.blocks = blocks;
        this.blockDataMap = blockDataMap;
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        // Cache the entry to be placed later during flush
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        long value = index;
        value = (value << 16) | blockStateId;
        value = (value << 16) | customBlockId;

        synchronized (blocks) {
            this.blocks.add(value);
        }

        if (data != null) {
            synchronized (blockDataMap) {
                this.blockDataMap.put(index, data);
            }
        }
    }

    @Override
    public void clear() {
        synchronized (blocks) {
            this.blocks.clear();
        }
    }

    /**
     * Apply this batch to chunk (0, 0).
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     */
    @Override
    public void apply(@NotNull InstanceContainer instance, @Nullable ChunkCallback callback) {
        apply(instance, 0, 0, callback);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunkX The x chunk coordinate of the target chunk
     * @param chunkZ The z chunk coordinate of the target chunk
     * @param callback The callback to be executed when the batch is applied.
     */
    public void apply(@NotNull InstanceContainer instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.", chunkX, chunkZ, instance.getUniqueId());
            return;
        }
        apply(instance, chunk, callback);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunk The target chunk
     * @param callback The callback to be executed when the batch is applied
     */
    public void apply(@NotNull InstanceContainer instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback) {
        apply(instance, chunk, callback, true);
    }

    /**
     * Apply this batch to the given chunk, and execute the callback
     * immediately when the blocks have been applied, in an unknown thread.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunk The target chunk
     * @param callback The callback to be executed when the batch is applied
     */
    public void unsafeApply(@NotNull InstanceContainer instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback) {
        apply(instance, chunk, callback, false);
    }

    /**
     * Apply this batch to the given chunk, and execute the callback depending on safeCallback.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunk The target chunk
     * @param callback The callback to be executed when the batch is applied
     * @param safeCallback If true, the callback will be executed in the next instance update. Otherwise it will be executed immediately upon completion
     */
    protected void apply(@NotNull InstanceContainer instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback, boolean safeCallback) {
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(instance, chunk, callback, safeCallback));
    }

    /**
     * Applies this batch in the current thread, executing the callback upon completion.
     */
    private void singleThreadFlush(InstanceContainer instance, Chunk chunk, @Nullable ChunkCallback callback, boolean safeCallback) {
        if (blocks.isEmpty()) {
            OptionalCallback.execute(callback, chunk);
            return;
        }

        if (!chunk.isLoaded()) {
            LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.", chunk.getChunkX(), chunk.getChunkZ(), instance.getUniqueId());
            return;
        }

        synchronized (blocks) {
            for (long block : blocks) {
                apply(chunk, block);
            }
        }

        updateChunk(instance, chunk, callback, safeCallback);
    }

    /**
     * Applies a single block change given a chunk and a value in the described format.
     *
     * @param chunk The chunk to apply the change
     * @param value block index|state id|custom block id (32|16|16 bits)
     */
    private void apply(@NotNull Chunk chunk, long value) {
        final short customBlockId = (short) (value & 0xFFFF);
        final short blockId = (short) ((value >> 16) & 0xFFFF);
        final int index = (int) ((value >> 32) & 0xFFFFFFFFL);

        Data data = null;
        if (!blockDataMap.isEmpty()) {
            synchronized (blockDataMap) {
                data = blockDataMap.get(index);
            }
        }

        chunk.UNSAFE_setBlock(ChunkUtils.blockIndexToChunkPositionX(index),
                ChunkUtils.blockIndexToChunkPositionY(index),
                ChunkUtils.blockIndexToChunkPositionZ(index),
                blockId, customBlockId, data, CustomBlockUtils.hasUpdate(customBlockId));
    }

    /**
     * Updates the given chunk for all of its viewers, and executes the callback.
     */
    private void updateChunk(@NotNull InstanceContainer instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback, boolean safeCallback) {
        // Refresh chunk for viewers

        // Formerly this had an option to do a Chunk#sendChunkUpdate
        // however Chunk#sendChunk does the same including a light update
        chunk.sendChunk();

        instance.refreshLastBlockChangeTime();

        if (callback != null) {
            if (safeCallback) {
                instance.scheduleNextTick(inst -> callback.accept(chunk));
            } else {
                callback.accept(chunk);
            }
        }
    }
}
