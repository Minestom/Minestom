package net.minestom.server.instance.batch;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockBatch implements InstanceBatch {
    private final InstanceContainer instance;
    private final BatchOption batchOption;

    // In the form of <Chunk Index, Batch>
    private final Map<Long, ChunkBatch> data = new HashMap<>();

    public BlockBatch(@NotNull InstanceContainer instance, @NotNull BatchOption batchOption) {
        this.instance = instance;
        this.batchOption = batchOption;
    }

    public BlockBatch(@NotNull InstanceContainer instance) {
        this(instance, new BatchOption());
    }

    @Override
    public synchronized void setBlockStateId(int x, int y, int z, short blockStateId, @Nullable Data data) {
        final Chunk chunk = this.instance.getChunkAt(x, z);
        addBlockData(chunk, x, y, z, blockStateId, (short) 0, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, @Nullable Data data) {
        final Chunk chunk = this.instance.getChunkAt(x, z);
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        addBlockData(chunk, x, y, z, customBlock.getDefaultBlockStateId(), customBlockId, data);
    }

    @Override
    public synchronized void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        final Chunk chunk = this.instance.getChunkAt(x, z);
        addBlockData(chunk, x, y, z, blockStateId, customBlockId, data);
    }

    private void addBlockData(@NotNull Chunk chunk, int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());

        ChunkBatch chunkBatch = this.data.get(chunkIndex);
        if (chunkBatch == null)
            chunkBatch = new ChunkBatch(this.instance, chunk, this.batchOption, false);

        int relativeX = x - (chunk.getChunkX() * Chunk.CHUNK_SIZE_X);
        int relativeZ = z - (chunk.getChunkZ() * Chunk.CHUNK_SIZE_Z);
        chunkBatch.setSeparateBlocks(relativeX, y, relativeZ, blockStateId, customBlockId, data);

        this.data.put(chunkIndex, chunkBatch);
    }

    public void flush(@Nullable Runnable callback) {
        synchronized (data) {
            AtomicInteger counter = new AtomicInteger();
            for (ChunkBatch chunkBatch : data.values()) {
                chunkBatch.flush(c -> {
                    final boolean isLast = counter.incrementAndGet() == data.size();

                    // Execute the callback if this was the last chunk to process
                    if (isLast) {
                        this.instance.refreshLastBlockChangeTime();
                        if (callback != null) {
                            this.instance.scheduleNextTick(inst -> callback.run());
                        }
                    }
                });
            }
        }
    }
}