package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
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
        final int index = CoordConversion.chunkBlockIndex(x, y, z);
        synchronized (blocks) {
            this.blocks.put(index, block);
        }
    }

    @Override
    public void clear() {
        synchronized (blocks) {
            this.blocks.clear();
        }
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
    @ApiStatus.Internal
    public @NotNull CompletableFuture<@Nullable ChunkBatch> apply(
            @NotNull Instance instance, int chunkX, int chunkZ, boolean subBatch) {
        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        CompletableFuture<Chunk> chunkFuture = CompletableFuture.completedFuture(chunk);

        if (chunk == null || !chunk.isLoaded()) {
            if (!instance.hasEnabledAutoChunkLoad()) {
                LOGGER.warn("Unable to apply ChunkBatch to unloaded chunk ({}, {}) in {}.",
                        chunkX, chunkZ, instance.getUniqueId());
                return CompletableFuture.completedFuture(null);
            }
            chunkFuture = instance.loadChunk(chunkX, chunkZ);
        }

        return chunkFuture.thenApplyAsync((targetChunk) -> applyLocal(instance, targetChunk, subBatch));
    }

    /**
     * Applies this batch in the current thread.
     * Target chunk should be loaded, callers should check this.
     */
    @Nullable
    private ChunkBatch applyLocal(Instance instance, Chunk chunk, boolean subBatch) {
        final ChunkBatch inverse = this.options.shouldCalculateInverse() ? new ChunkBatch(inverseOption) : null;

        if (this.options.isFullChunk()) {
            // Clear the chunk
            // FIXME: take inverse batch from before this
            chunk.reset();
        }

        if (blocks.isEmpty()) {
            // Nothing to flush
            return null;
        }

        try {
            synchronized (chunk) {
                synchronized (blocks) {
                    for (var entry : Int2ObjectMaps.fastIterable(blocks)) {
                        final int position = entry.getIntKey();
                        final Block block = entry.getValue();
                        applyBlock(chunk, position, block, inverse);
                    }
                }
            }

            updateChunk(instance, chunk, subBatch);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return inverse;
    }

    /**
     * Applies a single block change given a chunk and a value in the described format.
     *
     * @param chunk The chunk to apply the change
     * @param index the block position computed using {@link CoordConversion#chunkBlockIndex(int, int, int)}
     * @param block the block to place
     */
    private void applyBlock(@NotNull Chunk chunk, int index, Block block, @Nullable ChunkBatch inverse) {
        final int x = CoordConversion.chunkBlockIndexGetX(index);
        final int y = CoordConversion.chunkBlockIndexGetY(index);
        final int z = CoordConversion.chunkBlockIndexGetZ(index);
        if (inverse != null) {
            Block prevBlock = chunk.getBlock(x, y, z);
            inverse.setBlock(x, y, z, prevBlock);
        }
        chunk.setBlock(x, y, z, block);
    }

    private void updateChunk(@NotNull Instance instance, @NotNull Chunk chunk, boolean subBatch) {
        if (options.shouldSendUpdate()) {
            // update viewers
            chunk.sendChunk();

            // send light
            if (!subBatch && options.shouldSendLight()) {
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