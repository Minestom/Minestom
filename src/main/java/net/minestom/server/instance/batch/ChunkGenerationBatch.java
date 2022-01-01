package net.minestom.server.instance.batch;

import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkGenerationBatch extends ChunkBatch {
    private final InstanceContainer instance;
    private final Chunk chunk;

    public ChunkGenerationBatch(InstanceContainer instance, Chunk chunk) {
        super(new BatchOption());
        this.instance = instance;
        this.chunk = chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        chunk.setBlock(x, y, z, block);
    }

    public @NotNull CompletableFuture<@NotNull Chunk> generate(@NotNull ChunkGenerator chunkGenerator) {
        final CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
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
                this.instance.refreshLastBlockChangeTime();
                completableFuture.complete(chunk);
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
