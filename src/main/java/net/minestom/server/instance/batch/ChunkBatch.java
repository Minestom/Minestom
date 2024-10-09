package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * A Batch used when all the blocks changed are contained inside a single chunk.
 * If more than one chunk is needed, use an {@link AbsoluteBlockBatch} instead.
 * <p>
 * The batch can be placed in any chunk in any instance; however, it will always remain
 * aligned to a chunk border. If completely translatable block changes are needed, use a
 * {@link RelativeBlockBatch} instead.
 * <p>
 * Coordinates are relative to the chunk (0-15) instead of world coordinates.
 *
 * @see Batch
 */
public class ChunkBatch implements Batch {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBatch.class);

    private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();

    private final BatchOption options;
    private final BatchOption inverseOption;

    public ChunkBatch() {
        this(new BatchOption());
    }

    public ChunkBatch(@NotNull BatchOption options) {
        this(options, new BatchOption());
    }

    public ChunkBatch(@NotNull BatchOption options, @NotNull BatchOption inverseOption) {
        this.options = options;
        this.inverseOption = inverseOption;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        synchronized (blocks) {
            this.blocks.put(index, block);
        }
    }

    void UNSAFE_setBlock(int x, int y, int z, @NotNull Block block) {
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        this.blocks.put(index, block);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable ChunkBatch> apply(@NotNull Instance instance) {
        return apply(instance, 0, 0);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunkX   The x chunk coordinate of the target chunk
     * @param chunkZ   The z chunk coordinate of the target chunk
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable ChunkBatch> apply(
            @NotNull Instance instance, int chunkX, int chunkZ) {
        return apply(instance, chunkX, chunkZ, false);
    }

    /**
     * Apply this batch to the given chunk.
     *
     * @param instance The instance in which the batch should be applied
     * @param chunkX   The x chunk coordinate of the target chunk
     * @param chunkZ   The z chunk coordinate of the target chunk
     * @param subBatch Whether this batch is part of a bigger batch, used to optimize lighting updates.
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable ChunkBatch> apply(
            @NotNull Instance instance, int chunkX, int chunkZ, boolean subBatch) {
        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.",
                    chunkX, chunkZ, instance.getUniqueId());
            return CompletableFuture.completedFuture(null);
        }
        return apply(instance, chunk, subBatch);
    }

    /**
     * Apply this batch to the given chunk, and execute the callback depending on safeCallback.
     *
     * @param instance     The instance in which the batch should be applied
     * @param chunk        The target chunk
     * @return A completable future to the inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    protected @NotNull CompletableFuture<@Nullable ChunkBatch> apply(
            @NotNull Instance instance, @NotNull Chunk chunk, boolean subBatch) {
        return CompletableFuture.supplyAsync(() -> {
            final ChunkBatch inverse = this.options.shouldCalculateInverse() ? new ChunkBatch(inverseOption) : null;
            synchronized (chunk) {
                singleThreadFlush(instance, chunk, inverse, subBatch);
            }
            return inverse;
        });
    }

    /**
     * Applies this batch in the current thread.
     */
    private void singleThreadFlush(Instance instance, Chunk chunk, @Nullable ChunkBatch inverse, boolean subBatch) {
        try {
            if (!chunk.isLoaded()) {
                LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.",
                        chunk.getChunkX(), chunk.getChunkZ(), instance.getUniqueId());
                return;
            }

            if (this.options.isFullChunk()) {
                // Clear the chunk
                // TODO: take inverse batch from before this
                chunk.reset();
            }

            if (blocks.isEmpty()) {
                // Nothing to flush
                return;
            }

            synchronized (blocks) {
                for (var entry : Int2ObjectMaps.fastIterable(blocks)) {
                    final int position = entry.getIntKey();
                    final Block block = entry.getValue();
                    applyBlock(chunk, position, block, inverse);
                }
            }

            updateChunk(instance, chunk, subBatch);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Applies a single block change given a chunk and a value in the described format.
     *
     * @param chunk The chunk to apply the change
     * @param index the block position computed using {@link ChunkUtils#getBlockIndex(int, int, int)}
     * @param block the block to place
     */
    private void applyBlock(@NotNull Chunk chunk, int index, Block block, @Nullable ChunkBatch inverse) {
        final int x = ChunkUtils.blockIndexToChunkPositionX(index);
        final int y = ChunkUtils.blockIndexToChunkPositionY(index);
        final int z = ChunkUtils.blockIndexToChunkPositionZ(index);
        if (inverse != null) {
            Block prevBlock = chunk.getBlock(x, y, z);
            inverse.UNSAFE_setBlock(x, y, z, prevBlock);
        }
        chunk.setBlock(x, y, z, block);
    }

    private void updateChunk(@NotNull Instance instance, @NotNull Chunk chunk, boolean subBatch) {
        if (options.shouldSendUpdate()) {
            // update viewers
            chunk.sendChunk();

            // send light
            if (!subBatch) {
                int chunkX = chunk.getChunkX();
                int chunkZ = chunk.getChunkZ();
                for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                    for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                        if (instance.getChunk(x, z) instanceof LightingChunk lightingChunk) {
                            lightingChunk.sendLighting();
                        }
                    }
                }
            }
        }
        instance.refreshLastBlockChangeTime();
    }
}