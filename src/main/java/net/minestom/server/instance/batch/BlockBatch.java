package net.minestom.server.instance.batch;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.CustomBlock;

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
            addBlockData(chunk, x, y, z, false, blockId, (short) 0, data);
        }
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        synchronized (this) {
            Chunk chunk = this.instance.getChunkAt(x, z);
            CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(blockId);
            addBlockData(chunk, x, y, z, true, customBlock.getBlockId(), blockId, data);
        }
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockId, short customBlockId, Data data) {
        synchronized (this) {
            Chunk chunk = this.instance.getChunkAt(x, z);
            addBlockData(chunk, x, y, z, true, blockId, customBlockId, data);
        }
    }

    private void addBlockData(Chunk chunk, int x, int y, int z, boolean customBlock, short blockId, short customBlockId, Data data) {
        List<BlockData> blocksData = this.data.get(chunk);
        if (blocksData == null)
            blocksData = new ArrayList<>();

        BlockData blockData = new BlockData();
        blockData.x = x;
        blockData.y = y;
        blockData.z = z;
        blockData.hasCustomBlock = customBlock;
        blockData.blockId = blockId;
        blockData.customBlockId = customBlockId;
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

                    chunk.refreshDataPacket(() -> {
                        instance.sendChunkUpdate(chunk);
                    });

                    if (isLast) {
                        if (callback != null)
                            callback.run();
                    }

                }
            });
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
