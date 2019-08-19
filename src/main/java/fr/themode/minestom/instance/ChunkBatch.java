package fr.themode.minestom.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkBatch implements BlockModifier {

    private static volatile ExecutorService batchesPool = Executors.newFixedThreadPool(3);

    private Instance instance;
    private Chunk chunk;

    private List<BlockData> dataList = new ArrayList<>();

    public ChunkBatch(Instance instance, Chunk chunk) {
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, short blockId) {
        BlockData data = new BlockData();
        data.x = (byte) x;
        data.y = (byte) y;
        data.z = (byte) z;
        data.blockId = blockId;

        this.dataList.add(data);
    }

    @Override
    public void setBlock(int x, int y, int z, String blockId) {
        BlockData data = new BlockData();
        data.x = (byte) x;
        data.y = (byte) y;
        data.z = (byte) z;
        data.blockIdentifier = blockId;

        this.dataList.add(data);
    }

    public void flush() {
        synchronized (chunk) {
            batchesPool.submit(() -> {
                for (BlockData data : dataList) {
                    data.apply(chunk);
                }
                instance.sendChunkUpdate(chunk); // TODO partial chunk data
            });
        }
    }

    private class BlockData {

        private byte x, y, z;
        private short blockId;
        private String blockIdentifier;

        public void apply(Chunk chunk) {
            if (blockIdentifier == null) {
                chunk.setBlock(x, y, z, blockId);
            } else {
                chunk.setBlock(x, y, z, blockIdentifier);
            }
        }

    }

}
