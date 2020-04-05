package fr.themode.minestom.instance.batch;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.InstanceContainer;
import fr.themode.minestom.utils.SerializerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBatch implements InstanceBatch {

    private InstanceContainer instance;

    private Map<Chunk, List<BlockData>> data = new HashMap<>();

    public BlockBatch(InstanceContainer instance) {
        this.instance = instance;
    }

    @Override
    public void setBlock(int x, int y, int z, short blockId, Data data) {
        synchronized (this) {
            Chunk chunk = this.instance.getChunkAt(x, z);
            addBlockData(chunk, x, y, z, false, blockId, data);
        }
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        synchronized (this) {
            Chunk chunk = this.instance.getChunkAt(x, z);
            addBlockData(chunk, x, y, z, true, blockId, data);
        }
    }

    private void addBlockData(Chunk chunk, int x, int y, int z, boolean customBlock, short blockId, Data data) {
        List<BlockData> blocksData = this.data.get(chunk);
        if (blocksData == null)
            blocksData = new ArrayList<>();

        BlockData blockData = new BlockData();
        blockData.x = x % 16;
        blockData.y = y;
        blockData.z = z % 16;
        blockData.isCustomBlock = customBlock;
        blockData.blockId = blockId;
        blockData.data = data;

        blocksData.add(blockData);

        this.data.put(chunk, blocksData);
    }

    public void flush(Runnable callback) {
        int counter = 0;
        for (Map.Entry<Chunk, List<BlockData>> entry : data.entrySet()) {
            counter++;
            Chunk chunk = entry.getKey();
            List<BlockData> dataList = entry.getValue();
            boolean isLast = counter == data.size();
            batchesPool.execute(() -> {
                synchronized (chunk) {
                    for (BlockData data : dataList) {
                        data.apply(chunk);
                    }
                    chunk.refreshDataPacket();
                    instance.sendChunkUpdate(chunk);
                    if (isLast) {
                        // data.clear();
                        if (callback != null)
                            callback.run();
                    }
                }
            });
        }
    }

    private class BlockData {

        private int x, y, z;
        private boolean isCustomBlock;
        private short blockId;
        private Data data;

        public void apply(Chunk chunk) {
            int index = SerializerUtils.chunkCoordToIndex((byte) x, (byte) y, (byte) z);
            if (!isCustomBlock) {
                chunk.UNSAFE_setBlock(index, blockId, data);
            } else {
                chunk.UNSAFE_setCustomBlock(index, blockId, data);
            }
        }

    }

}
