package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.*;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
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
    private final BatchOption inverseOption;

    public AbsoluteBlockBatch() {
        this(new BatchOption());
    }

    public AbsoluteBlockBatch(@NotNull BatchOption options) {
        this(options, new BatchOption());
    }

    public AbsoluteBlockBatch(@NotNull BatchOption options, @NotNull BatchOption inverseOption) {
        this.options = options;
        this.inverseOption = inverseOption;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int chunkX = CoordConversion.globalToChunk(x);
        final int chunkZ = CoordConversion.globalToChunk(z);
        final long chunkIndex = CoordConversion.chunkIndex(chunkX, chunkZ);

        final ChunkBatch chunkBatch;
        synchronized (chunkBatchesMap) {
            chunkBatch = chunkBatchesMap.computeIfAbsent(chunkIndex, i -> new ChunkBatch(this.options, this.inverseOption));
        }

        chunkBatch.setBlock(x, y, z, block);
    }

    @Override
    public void clear() {
        synchronized (chunkBatchesMap) {
            chunkBatchesMap.clear();
        }
    }

    /**
     * Applies this batch to the given instance.
     *
     * @param instance The instance in which the batch should be applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance) {
        final List<CompletableFuture<Void>> chunkBatchFutures = new ArrayList<>();
        final LongList chunkBatchIndexes = new LongArrayList();
        final AbsoluteBlockBatch inverse = this.options.shouldCalculateInverse() ?
                new AbsoluteBlockBatch(inverseOption) : null;

        synchronized (chunkBatchesMap) {
            for (var entry : Long2ObjectMaps.fastIterable(chunkBatchesMap)) {
                final long chunkIndex = entry.getLongKey();
                final int chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                final int chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                final ChunkBatch batch = entry.getValue();

                final var future = batch.apply(instance, chunkX, chunkZ, true)
                        .thenApply((chunkInverse) -> {
                            if (chunkInverse == null || inverse == null) {
                                return null;
                            }
                            synchronized (inverse.chunkBatchesMap) {
                                inverse.chunkBatchesMap.put(chunkIndex, chunkInverse);
                            }

                            return (Void) null;
                        });

                chunkBatchFutures.add(future);
                chunkBatchIndexes.add(chunkIndex);
            }
        }

        return CompletableFuture.allOf(chunkBatchFutures.toArray(new CompletableFuture[0])).thenApplyAsync(v -> {
            sendLighting(instance, chunkBatchIndexes);

            return inverse;
        });
    }

    /**
     * Send light updates to viewers of LightingChunks
     */
    protected void sendLighting(@NotNull Instance instance, @NotNull LongList chunkIndexes) {
        if (options.shouldSendUpdate() && options.shouldSendLight()) {
            // expand to include surrounding chunks
            final LongSet expandedIndexes = new LongOpenHashSet();
            for (long index : chunkIndexes) {
                int chunkX = CoordConversion.chunkIndexGetX(index);
                int chunkZ = CoordConversion.chunkIndexGetZ(index);
                for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                    for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                        expandedIndexes.add(CoordConversion.chunkIndex(x, z));
                    }
                }
            }

            for (long chunkIndex : expandedIndexes) {
                int chunkX = CoordConversion.chunkIndexGetX(chunkIndex);
                int chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                if (instance.getChunk(chunkX, chunkZ) instanceof LightingChunk lightingChunk) {
                    lightingChunk.sendLighting();
                }
            }
        }
    }
}
