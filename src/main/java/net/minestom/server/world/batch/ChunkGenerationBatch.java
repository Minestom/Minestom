package net.minestom.server.world.batch;

import net.minestom.server.block.Block;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.world.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChunkGenerationBatch extends ChunkBatch {
    private final WorldContainer worldContainer;
    private final Chunk chunk;

    public ChunkGenerationBatch(WorldContainer worldContainer, Chunk chunk) {
        super(new BatchOption());
        this.worldContainer = worldContainer;
        this.chunk = chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        chunk.setBlock(x, y, z, block);
    }

    public void generate(@NotNull ChunkGenerator chunkGenerator, @Nullable ChunkCallback callback) {
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
                this.worldContainer.refreshLastBlockChangeTime();
                if (callback != null)
                    this.worldContainer.scheduleNextTick(inst -> callback.accept(this.chunk));
            }
        });
    }

    @Override
    public void clear() {
        throw new IllegalStateException("#clear is not supported for chunk generation batch.");
    }

    @Override
    protected ChunkBatch apply(@NotNull World world, @NotNull Chunk chunk, @Nullable ChunkCallback callback, boolean safeCallback) {
        throw new IllegalStateException("#apply is not supported for chunk generation batch.");
    }
}
