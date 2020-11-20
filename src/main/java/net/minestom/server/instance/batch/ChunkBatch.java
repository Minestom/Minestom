package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.data.Data;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Used when all the blocks you want to place can be contained within only one {@link Chunk},
 * use a {@link BlockBatch} instead otherwise.
 * Can be created using {@link Instance#createChunkBatch(Chunk)}.
 * <p>
 * Use chunk coordinate (0-15) instead of world's.
 *
 * @see InstanceBatch
 */
public class ChunkBatch implements InstanceBatch {

    private final InstanceContainer instance;
    private final Chunk chunk;

    // Need to be synchronized manually
    // Format: blockIndex/blockStateId/customBlockId (32/16/16 bits)
    private final LongList blocks = new LongArrayList();

    // Need to be synchronized manually
    // block index - data
    private final Int2ObjectMap<Data> blockDataMap = new Int2ObjectOpenHashMap<>();

    public ChunkBatch(@NotNull InstanceContainer instance, @NotNull Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, @Nullable Data data) {
        addBlockData((byte) x, y, (byte) z, blockStateId, (short) 0, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, @Nullable Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block with the id " + customBlockId + " does not exist!");
        addBlockData((byte) x, y, (byte) z, customBlock.getDefaultBlockStateId(), customBlockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        addBlockData((byte) x, y, (byte) z, blockStateId, customBlockId, data);
    }

    private void addBlockData(byte x, int y, byte z, short blockStateId, short customBlockId, @Nullable Data data) {
        final int index = ChunkUtils.getBlockIndex(x, y, z);

        if (data != null) {
            synchronized (blockDataMap) {
                this.blockDataMap.put(index, data);
            }
        }

        long value = index;
        value = (value << 16) | blockStateId;
        value = (value << 16) | customBlockId;

        synchronized (blocks) {
            this.blocks.add(value);
        }
    }

    /**
     * Called to fill the chunk batch.
     *
     * @param chunkGenerator the chunk generator
     * @param callback       the optional callback executed once the batch is done
     */
    public void flushChunkGenerator(@NotNull ChunkGenerator chunkGenerator, @Nullable ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> {
            final List<ChunkPopulator> populators = chunkGenerator.getPopulators();
            final boolean hasPopulator = populators != null && !populators.isEmpty();

            chunkGenerator.generateChunkData(this, chunk.getChunkX(), chunk.getChunkZ());

            // Check if there is anything to process
            if (blocks.isEmpty() && !hasPopulator) {
                OptionalCallback.execute(callback, chunk);
                return;
            }

            singleThreadFlush(hasPopulator ? null : callback, true);

            clearData(); // So the populators won't place those blocks again

            if (hasPopulator) {
                for (ChunkPopulator chunkPopulator : populators) {
                    chunkPopulator.populateChunk(this, chunk);
                }
                singleThreadFlush(callback, true);

                clearData(); // Clear populators blocks
            }
        });
    }

    /**
     * Executes the batch in the dedicated pool and run the callback during the next instance update when blocks are placed
     * (which means that the callback can be called 50ms after, but in a determinable thread).
     *
     * @param callback the callback to execute once the blocks are placed
     */
    public void flush(@Nullable ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(callback, true));
    }

    /**
     * Executes the batch in the dedicated pool and run the callback once blocks are placed (in the block batch pool).
     * <p>
     * So the callback is executed in an unexpected thread, but you are sure that it will be called immediately.
     *
     * @param callback the callback to execute once the blocks are placed
     */
    public void unsafeFlush(@Nullable ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(callback, false));
    }

    /**
     * Resets the chunk batch by removing all the entries.
     */
    public void clearData() {
        synchronized (blocks) {
            this.blocks.clear();
        }
    }

    /**
     * Executes the batch in the current thread.
     *
     * @param callback     the callback to execute once the blocks are placed
     * @param safeCallback true to run the callback in the instance update thread, otherwise run in the current one
     */
    private void singleThreadFlush(@Nullable ChunkCallback callback, boolean safeCallback) {
        if (blocks.isEmpty()) {
            OptionalCallback.execute(callback, chunk);
            return;
        }

        synchronized (chunk) {
            if (!chunk.isLoaded())
                return;

            synchronized (blocks) {
                for (long block : blocks) {
                    apply(chunk, block);
                }
            }

            // Refresh chunk for viewers
            chunk.sendChunkUpdate();

            if (callback != null) {
                this.instance.refreshLastBlockChangeTime();

                if (safeCallback) {
                    this.instance.scheduleNextTick(inst -> callback.accept(chunk));
                } else {
                    callback.accept(chunk);
                }
            }
        }
    }

    /**
     * Places a block which is encoded in a long.
     *
     * @param chunk the chunk to place the block on
     * @param value the block data
     */
    private void apply(@NotNull Chunk chunk, long value) {
        final short customBlockId = (short) (value & 0xFF);
        final short blockId = (short) (value >> 16 & 0xFF);
        final int index = (int) (value >> 32 & 0xFFFF);

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

}
