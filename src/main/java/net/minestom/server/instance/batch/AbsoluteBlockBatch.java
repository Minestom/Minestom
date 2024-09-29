package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final BatchOption options;

    private volatile BatchOption inverseOption = new BatchOption();

    public AbsoluteBlockBatch() {
        this(new BatchOption());
    }

    public AbsoluteBlockBatch(BatchOption options) {
        this.options = options;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int chunkX = ChunkUtils.getChunkCoordinate(x);
        final int chunkZ = ChunkUtils.getChunkCoordinate(z);
        final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);

        final ChunkBatch chunkBatch;
        synchronized (chunkBatchesMap) {
            chunkBatch = chunkBatchesMap.computeIfAbsent(chunkIndex, i -> new ChunkBatch(this.options));
        }

        chunkBatch.setBlock(x, y, z, block);
    }

    @Override
    public void clear() {
        synchronized (chunkBatchesMap) {
            this.chunkBatchesMap.clear();
        }
    }
    /**
     * Applies this batch to the given instance.
     *
     * @param instance The instance in which the batch should be applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance) {
        final List<CompletableFuture<ChunkBatch>> chunkBatchFutures = new ArrayList<>();
        final LongList chunkBatchIndexes = new LongArrayList();

        synchronized (chunkBatchesMap) {
            for (var entry : Long2ObjectMaps.fastIterable(chunkBatchesMap)) {
                final long chunkIndex = entry.getLongKey();
                final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
                final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
                final ChunkBatch batch = entry.getValue();

                chunkBatchFutures.add(batch.apply(instance, chunkX, chunkZ));
                chunkBatchIndexes.add(chunkIndex);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            // await chunkBatch futures and collect inverse batches
            final AbsoluteBlockBatch inverse = this.options.shouldCalculateInverse() ?
                    new AbsoluteBlockBatch(inverseOption) : null;

            for (int index = 0; index < chunkBatchIndexes.size(); index++) {
                final long chunkIndex = chunkBatchIndexes.getLong(index);
                final ChunkBatch chunkInverse = chunkBatchFutures.get(index).join();
                if (inverse != null) {
                    inverse.chunkBatchesMap.put(chunkIndex, chunkInverse);
                }
            }

            instance.refreshLastBlockChangeTime();
            // TODO: send light updates?

            return inverse;
        });
    }

    public @NotNull BatchOption getInverseOption() {
        return inverseOption;
    }

    public void setInverseOption(@NotNull BatchOption inverseOption) {
        this.inverseOption = inverseOption;
    }
}
