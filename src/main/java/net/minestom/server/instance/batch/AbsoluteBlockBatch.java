package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link Batch} which can be used when changes are required across chunk borders,
 * but the changes do not need any translation. If translation is required,
 * use a {@link RelativeBlockBatch} instead.
 * <p>
 * Coordinates are relative to the world origin.
 *
 * @see Batch
 * @see RelativeBlockBatch
 */
public class AbsoluteBlockBatch implements Batch {

    // In the form of <Chunk Index, Batch>
    private final Long2ObjectMap<ChunkBatch> chunkBatchesMap = new Long2ObjectOpenHashMap<>();

    public AbsoluteBlockBatch() {
    }

    private ChunkBatch get(long index) {
        return chunkBatchesMap.computeIfAbsent(index, i -> new ChunkBatch());
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int chunkX = ChunkUtils.getChunkCoordinate(x);
        final int chunkZ = ChunkUtils.getChunkCoordinate(z);
        final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);

        final ChunkBatch chunkBatch;
        synchronized (chunkBatchesMap) {
            chunkBatch = get(chunkIndex);
        }

        final int relativeX = x - (chunkX * Chunk.SIZE_X);
        final int relativeZ = z - (chunkZ * Chunk.SIZE_Z);
        chunkBatch.setBlock(relativeX, y, relativeZ, block);
    }

    @Override
    public void clear() {
        synchronized (chunkBatchesMap) {
            this.chunkBatchesMap.clear();
        }
    }

    /**
     * Applies this batch to the given instance
     *
     * @param instance     The instance in which the batch should be applied
     *                     Otherwise it will be executed immediately upon completion
     */
    public CompletableFuture<Void> apply(@NotNull Instance instance) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (var entry : chunkBatchesMap.long2ObjectEntrySet()) {
            long chunkIndex = entry.getLongKey();
            ChunkBatch chunkBatch = entry.getValue();
            int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

            futures.add(chunkBatch.apply(instance, chunkX, chunkZ));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
