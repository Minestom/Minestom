package fr.themode.minestom.instance.batch;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.BlockModifier;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBatch implements IBatch, BlockModifier {

    private InstanceContainer instance;

    private Map<Chunk, List<BlockData>> data = new HashMap<>();

    public BlockBatch(InstanceContainer instance) {
        this.instance = instance;
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, short blockId, Data data) {
        Chunk chunk = this.instance.getChunkAt(x, z);
        List<BlockData> blocksData = this.data.getOrDefault(chunk, new ArrayList<>());

        BlockData blockData = new BlockData();
        blockData.x = x % 16;
        blockData.y = y;
        blockData.z = z % 16;
        blockData.blockId = blockId;
        blockData.data = data;

        blocksData.add(blockData);

        this.data.put(chunk, blocksData);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        Chunk chunk = this.instance.getChunkAt(x, z);
        List<BlockData> blocksData = this.data.getOrDefault(chunk, new ArrayList<>());

        BlockData blockData = new BlockData();
        blockData.x = x % 16;
        blockData.y = y;
        blockData.z = z % 16;
        blockData.isCustomBlock = true;
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
            if (!isCustomBlock) {
                chunk.UNSAFE_setBlock((byte) x, (byte) y, (byte) z, blockId, data);
            } else {
                chunk.UNSAFE_setCustomBlock((byte) x, (byte) y, (byte) z, blockId, data);
            }
        }

    }

}
