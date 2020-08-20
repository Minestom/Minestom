package net.minestom.server.instance.batch;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.CustomBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Use chunk coordinate (0-16) instead of world's
 */
public class ChunkBatch implements InstanceBatch {

    private final InstanceContainer instance;
    private final Chunk chunk;

    // Give it the max capacity by default (avoid resizing)
    private List<BlockData> dataList =
            Collections.synchronizedList(new ArrayList<>(
                    Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SIZE_Y * Chunk.CHUNK_SIZE_Z));

    public ChunkBatch(InstanceContainer instance, Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, Data data) {
        addBlockData((byte) x, y, (byte) z, false, blockStateId, (short) 0, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        addBlockData((byte) x, y, (byte) z, true, customBlock.getBlockStateId(), customBlockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        addBlockData((byte) x, y, (byte) z, true, blockStateId, customBlockId, data);
    }

    private void addBlockData(byte x, int y, byte z, boolean customBlock, short blockStateId, short customBlockId, Data data) {
        // TODO store a single long with bitwise operators (xyz;boolean,short,short,boolean) with the data in a map
        BlockData blockData = new BlockData();
        blockData.x = x;
        blockData.y = y;
        blockData.z = z;
        blockData.hasCustomBlock = customBlock;
        blockData.blockStateId = blockStateId;
        blockData.customBlockId = customBlockId;
        blockData.data = data;

        this.dataList.add(blockData);
    }

    public void flushChunkGenerator(ChunkGenerator chunkGenerator, Consumer<Chunk> callback) {
        batchesPool.execute(() -> {
            final List<ChunkPopulator> populators = chunkGenerator.getPopulators();
            final boolean hasPopulator = populators != null && !populators.isEmpty();

            chunkGenerator.generateChunkData(this, chunk.getChunkX(), chunk.getChunkZ());
            singleThreadFlush(hasPopulator ? null : callback);

            clearData(); // So the populators won't place those blocks again

            if (hasPopulator) {
                for (ChunkPopulator chunkPopulator : populators) {
                    chunkPopulator.populateChunk(this, chunk);
                }
                singleThreadFlush(callback);

                clearData(); // Clear populators blocks
            }
        });
    }

    public void flush(Consumer<Chunk> callback) {
        batchesPool.execute(() -> {
            singleThreadFlush(callback);
        });
    }

    public void clearData() {
        dataList.clear();
    }

    private void singleThreadFlush(Consumer<Chunk> callback) {
        synchronized (chunk) {
            if (!chunk.isLoaded())
                return;

            for (BlockData data : dataList) {
                data.apply(chunk);
            }

            // Refresh chunk for viewers
            chunk.sendChunkUpdate();

            if (callback != null)
                callback.accept(chunk);
        }
    }

    private static class BlockData {

        private int x, y, z;
        private boolean hasCustomBlock;
        private short blockStateId;
        private short customBlockId;
        private Data data;

        public void apply(Chunk chunk) {
            if (!hasCustomBlock) {
                chunk.UNSAFE_setBlock(x, y, z, blockStateId, data);
            } else {
                chunk.UNSAFE_setCustomBlock(x, y, z, blockStateId, customBlockId, data);
            }
        }

    }

}
