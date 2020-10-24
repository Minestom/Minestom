package net.minestom.server.instance.batch;

import kotlin.collections.ArrayDeque;
import net.minestom.server.data.Data;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.chunk.ChunkCallback;
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

    private static final int INITIAL_SIZE = (Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SIZE_Y * Chunk.CHUNK_SIZE_Z) / 2;

    private final InstanceContainer instance;
    private final Chunk chunk;

    // Need to be synchronized manually
    private final ArrayDeque<BlockData> dataList = new ArrayDeque<>(INITIAL_SIZE);

    public ChunkBatch(InstanceContainer instance, Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, Data data) {
        addBlockData((byte) x, y, (byte) z, blockStateId, (short) 0, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        addBlockData((byte) x, y, (byte) z, customBlock.getDefaultBlockStateId(), customBlockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        addBlockData((byte) x, y, (byte) z, blockStateId, customBlockId, data);
    }

    private void addBlockData(byte x, int y, byte z, short blockStateId, short customBlockId, Data data) {
        // TODO store a single long with bitwise operators (xyz;boolean,short,short,boolean) with the data in a map
        final BlockData blockData = new BlockData(x, y, z, blockStateId, customBlockId, data);
        synchronized (dataList) {
            this.dataList.add(blockData);
        }
    }

    public void flushChunkGenerator(ChunkGenerator chunkGenerator, @Nullable ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> {
            final List<ChunkPopulator> populators = chunkGenerator.getPopulators();
            final boolean hasPopulator = populators != null && !populators.isEmpty();

            chunkGenerator.generateChunkData(this, chunk.getChunkX(), chunk.getChunkZ());
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
        synchronized (dataList) {
            this.dataList.clear();
        }
    }

    /**
     * Executes the batch in the current thread.
     *
     * @param callback     the callback to execute once the blocks are placed
     * @param safeCallback true to run the callback in the instance update thread, otherwise run in the current one
     */
    private void singleThreadFlush(@Nullable ChunkCallback callback, boolean safeCallback) {
        synchronized (chunk) {
            if (!chunk.isLoaded())
                return;

            synchronized (dataList) {
                for (BlockData data : dataList) {
                    data.apply(chunk);
                }
            }

            // Refresh chunk for viewers
            chunk.sendChunkUpdate();

            if (callback != null) {
                if (safeCallback) {
                    instance.scheduleNextTick(inst -> callback.accept(chunk));
                } else {
                    callback.accept(chunk);
                }
            }
        }
    }

    private static class BlockData {

        private final int x, y, z;
        private final short blockStateId;
        private final short customBlockId;
        private final Data data;

        private BlockData(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockStateId = blockStateId;
            this.customBlockId = customBlockId;
            this.data = data;
        }

        public void apply(Chunk chunk) {
            chunk.UNSAFE_setBlock(x, y, z, blockStateId, customBlockId, data, CustomBlockUtils.hasUpdate(customBlockId));
        }

    }

}
