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

    private InstanceContainer instance;
    private Chunk chunk;

    // Give it the max capacity by default (avoid resizing)
    private List<BlockData> dataList =
            Collections.synchronizedList(new ArrayList<>(
                    Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SIZE_Y * Chunk.CHUNK_SIZE_Z));

    public ChunkBatch(InstanceContainer instance, Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, short blockId, Data data) {
        addBlockData((byte) x, y, (byte) z, false, blockId, (short) 0, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(blockId);
        addBlockData((byte) x, y, (byte) z, true, customBlock.getBlockId(), blockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockId, short customBlockId, Data data) {
        addBlockData((byte) x, y, (byte) z, true, blockId, customBlockId, data);
    }

    private void addBlockData(byte x, int y, byte z, boolean customBlock, short blockId, short customBlockId, Data data) {
        BlockData blockData = new BlockData();
        blockData.x = x;
        blockData.y = y;
        blockData.z = z;
        blockData.hasCustomBlock = customBlock;
        blockData.blockId = blockId;
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
                Iterator<ChunkPopulator> populatorIterator = populators.iterator();
                while (populatorIterator.hasNext()) {
                    final ChunkPopulator chunkPopulator = populatorIterator.next();
                    chunkPopulator.populateChunk(this, chunk);
                }
                singleThreadFlush(callback);
            }
            clearData(); // Clear populators blocks
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

            chunk.refreshDataPacket(() -> {
                instance.sendChunkUpdate(chunk);
            });

            if (callback != null)
                callback.accept(chunk);
        }
    }

    private class BlockData {

        private int x, y, z;
        private boolean hasCustomBlock;
        private short blockId;
        private short customBlockId;
        private Data data;

        public void apply(Chunk chunk) {
            if (!hasCustomBlock) {
                chunk.UNSAFE_setBlock(x, y, z, blockId, data);
            } else {
                chunk.UNSAFE_setCustomBlock(x, y, z, blockId, customBlockId, data);
            }
        }

    }

}
