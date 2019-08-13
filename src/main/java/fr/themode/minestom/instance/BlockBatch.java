package fr.themode.minestom.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockBatch {

    private static volatile ExecutorService batchesPool = Executors.newFixedThreadPool(2);

    private Instance instance;

    private Map<Chunk, List<BlockData>> data = new HashMap<>();

    public BlockBatch(Instance instance) {
        this.instance = instance;
    }

    public synchronized void setBlock(int x, int y, int z, Block block) {
        final int chunkX = Math.floorDiv(x, 16);
        final int chunkZ = Math.floorDiv(z, 16);
        Chunk chunk = this.instance.getChunk(chunkX, chunkZ);
        if (chunk == null)
            chunk = this.instance.createChunk(Biome.VOID, chunkX, chunkZ);
        List<BlockData> blockData = this.data.getOrDefault(chunk, new ArrayList<>());

        BlockData data = new BlockData();
        data.x = x % 16;
        data.y = y;
        data.z = z % 16;
        data.block = block;

        blockData.add(data);

        this.data.put(chunk, blockData);
    }

    public void flush() {
        for (Map.Entry<Chunk, List<BlockData>> entry : data.entrySet()) {
            Chunk chunk = entry.getKey();
            List<BlockData> dataList = entry.getValue();
            synchronized (chunk) {
                batchesPool.submit(() -> {
                    for (BlockData data : dataList) {
                        data.apply(chunk);
                    }
                    instance.sendChunkUpdate(chunk); // TODO partial chunk data
                });
            }
        }
    }

    private class BlockData {

        private int x, y, z;
        private Block block;

        public void apply(Chunk chunk) {
            chunk.setBlock(x, y, z, block);
        }

    }

}
