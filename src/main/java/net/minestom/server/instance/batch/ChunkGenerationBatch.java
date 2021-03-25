package net.minestom.server.instance.batch;

import net.minestom.server.data.Data;
import net.minestom.server.instance.*;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkGenerationBatch extends ChunkBatch {
    private final InstanceContainer instance;
    private final Chunk chunk;

    public ChunkGenerationBatch(InstanceContainer instance, Chunk chunk) {
        super(null, null, new BatchOption());

        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data) {
        chunk.UNSAFE_setBlock(x, y, z, blockStateId, customBlockId, data, CustomBlockUtils.hasUpdate(customBlockId));
    }

    @NotNull
    public CompletableFuture<Chunk> generate(@NotNull ChunkGenerator chunkGenerator) {
        CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
        BLOCK_BATCH_POOL.execute(() -> {
            synchronized (chunk) {
                final List<ChunkPopulator> populators = chunkGenerator.getPopulators();
                final boolean hasPopulator = populators != null && !populators.isEmpty();

                chunkGenerator.generateChunkData(this, chunk.getChunkX(), chunk.getChunkZ());

                if (hasPopulator) {
                    for (ChunkPopulator chunkPopulator : populators) {
                        chunkPopulator.populateChunk(this, chunk);
                    }
                }

                // Update the chunk.
                this.chunk.sendChunk();
                this.instance.refreshLastBlockChangeTime();
                this.instance.scheduleNextTick(inst -> completableFuture.complete(chunk));
            }
        });
        return completableFuture;
    }

    @Override
    public void clear() {
        throw new IllegalStateException("#clear is not supported for chunk generation batch.");
    }

    @Override
    protected ChunkBatch apply(@NotNull Instance instance, @NotNull Chunk chunk, @Nullable ChunkCallback callback, boolean safeCallback) {
        throw new IllegalStateException("#apply is not supported for chunk generation batch.");
    }
}
