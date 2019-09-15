package fr.themode.minestom.instance.batch;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.BlockModifier;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.ChunkGenerator;
import fr.themode.minestom.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Use chunk coordinate (0-16) instead of world's
 */
public class ChunkBatch implements IBatch, BlockModifier {

    private InstanceContainer instance;
    private Chunk chunk;

    private List<BlockData> dataList = new ArrayList<>();

    public ChunkBatch(InstanceContainer instance, Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, short blockId, Data data) {
        BlockData blockData = new BlockData();
        blockData.x = (byte) x;
        blockData.y = (byte) y;
        blockData.z = (byte) z;
        blockData.isCustomBlock = false;
        blockData.blockId = blockId;
        blockData.data = data;

        this.dataList.add(blockData);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        BlockData blockData = new BlockData();
        blockData.x = (byte) x;
        blockData.y = (byte) y;
        blockData.z = (byte) z;
        blockData.isCustomBlock = true;
        blockData.blockId = blockId;
        blockData.data = data;

        this.dataList.add(blockData);
    }

    public void flushChunkGenerator(ChunkGenerator chunkGenerator, Consumer<Chunk> callback) {
        batchesPool.execute(() -> {
            chunkGenerator.generateChunkData(this, chunk.getChunkX(), chunk.getChunkZ());
            singleThreadFlush(callback);
        });
    }

    public void flush(Consumer<Chunk> callback) {
        batchesPool.execute(() -> {
            singleThreadFlush(callback);
        });
    }

    private void singleThreadFlush(Consumer<Chunk> callback) {
        synchronized (chunk) {
            for (BlockData data : dataList) {
                data.apply(chunk);
            }

            chunk.refreshDataPacket();
            instance.sendChunkUpdate(chunk);
            if (callback != null)
                callback.accept(chunk);
        }
    }

    private class BlockData {

        private byte x, y, z;
        private boolean isCustomBlock;
        private short blockId;
        private Data data;

        public void apply(Chunk chunk) {
            if (!isCustomBlock) {
                chunk.UNSAFE_setBlock(x, y, z, blockId, data);
            } else {
                chunk.UNSAFE_setCustomBlock(x, y, z, blockId, data);
            }
        }

    }

}
