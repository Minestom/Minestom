package fr.themode.minestom.instance;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Use chunk coordinate (0-16) instead of world's
 */
public class ChunkBatch implements BlockModifier {

    private static final ExecutorService batchesPool = new MinestomThread(Main.THREAD_COUNT_CHUNK_BATCH, "Ms-ChunkBatchPool");

    private InstanceContainer instance;
    private Chunk chunk;

    private List<BlockData> dataList = new ArrayList<>();

    public ChunkBatch(InstanceContainer instance, Chunk chunk) {
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

            // dataList.clear();
            chunk.refreshDataPacket();
            instance.sendChunkUpdate(chunk);
            if (callback != null)
                callback.accept(chunk);
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
                chunk.setCustomBlock(x, y, z, blockIdentifier);
            }
        }

    }

}
