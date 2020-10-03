package net.minestom.server.instance.batch;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.chunk.ChunkCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use chunk coordinate (0-16) instead of world's
 */
public class ChunkBatch implements InstanceBatch {

    private final InstanceContainer instance;
    private final Chunk chunk;

    // Give it the max capacity by default (avoid resizing)
    private final List<BlockData> dataList =
            Collections.synchronizedList(new ArrayList<>(
                    Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SIZE_Y * Chunk.CHUNK_SIZE_Z));

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
        BlockData blockData = new BlockData();
        blockData.x = x;
        blockData.y = y;
        blockData.z = z;
        blockData.blockStateId = blockStateId;
        blockData.customBlockId = customBlockId;
        blockData.data = data;

        this.dataList.add(blockData);
    }

    public void flushChunkGenerator(ChunkGenerator chunkGenerator, ChunkCallback callback) {
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
     * Execute the batch in the dedicated pool and run the callback during the next instance update when blocks are placed
     * (which means that the callback can be called 50ms after, and in a determinable thread)
     *
     * @param callback the callback to execute once the blocks are placed
     */
    public void flush(ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(callback, true));
    }

    /**
     * Execute the batch in the dedicated pool and run the callback once blocks are placed (in the block batch pool)
     *
     * @param callback the callback to execute once the blocks are placed
     */
    public void unsafeFlush(ChunkCallback callback) {
        BLOCK_BATCH_POOL.execute(() -> singleThreadFlush(callback, false));
    }

    public void clearData() {
        dataList.clear();
    }

    /**
     * Execute the batch in the current thread
     *
     * @param callback     the callback to execute once the blocks are placed
     * @param safeCallback true to run the callback in the instance update thread, otherwise run in the current one
     */
    private void singleThreadFlush(ChunkCallback callback, boolean safeCallback) {
        synchronized (dataList) {
            synchronized (chunk) {
                if (!chunk.isLoaded())
                    return;

                for (BlockData data : dataList) {
                    data.apply(chunk);
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
    }

    private static class BlockData {

        private int x, y, z;
        private short blockStateId;
        private short customBlockId;
        private Data data;

        public void apply(Chunk chunk) {
            chunk.setBlock(x, y, z, blockStateId, customBlockId, data, CustomBlockUtils.hasUpdate(customBlockId));
        }

    }

}
