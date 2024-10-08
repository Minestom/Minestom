package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.LocationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link Batch} which can be used when changes are required across chunk borders, and
 * are going to be reused in different places. If translation is not required, {@link AbsoluteBlockBatch}
 * should be used instead for efficiency purposes.
 * <p>
 * All inverses are {@link AbsoluteBlockBatch}s and represent the inverse of the application
 * at the position which it was applied.
 * <p>
 * If a batch will be used multiple times at the same coordinate, it is suggested
 * to convert it to an {@link AbsoluteBlockBatch} and cache the result. Application
 * of absolute batches (currently) is significantly faster than their relative counterpart.
 *
 * @see Batch
 * @see AbsoluteBlockBatch
 */
public class RelativeBlockBatch implements Batch {
    // Need to be synchronized manually
    // Format: global position index - block
    private final Long2ObjectMap<Block> blockIdMap = new Long2ObjectOpenHashMap<>();

    private final BatchOption options;
    private final BatchOption inverseOption;

    public RelativeBlockBatch() {
        this(new BatchOption());
    }

    public RelativeBlockBatch(@NotNull BatchOption options) {
        this(options, new BatchOption());
    }

    public RelativeBlockBatch(@NotNull BatchOption options, @NotNull BatchOption inverseOption) {
        this.options = options;
        this.inverseOption = inverseOption;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        LocationUtils.verifyPositionInIndexBounds(x, y, z);
        final long index = LocationUtils.getGlobalBlockIndex(x, y, z);

        synchronized (blockIdMap) {
            this.blockIdMap.put(index, block);
        }
    }

    /**
     * Applies this batch to the given instance at the origin (0, 0, 0) of the instance.
     *
     * @param instance The instance in which the batch should be applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Override
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance) {
        return apply(instance, 0, 0, 0);
    }

    /**
     * Applies this batch to the given instance at the given block position.
     *
     * @param instance The instance in which the batch should be applied
     * @param position The position to apply the batch
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance, @NotNull Point position) {
        return apply(instance, position.blockX(), position.blockY(), position.blockZ());
    }

    /**
     * Applies this batch to the given instance at the given position.
     *
     * @param instance     The instance in which the batch should be applied
     * @param x            The x position to apply the batch
     * @param y            The y position to apply the batch
     * @param z            The z position to apply the batch
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    public @NotNull CompletableFuture<@Nullable AbsoluteBlockBatch> apply(@NotNull Instance instance, int x, int y, int z) {
        return CompletableFuture.supplyAsync(() -> this.toAbsoluteBatch(x, y, z).apply(instance).join());
    }

    /**
     * Converts this batch to an absolute batch at the origin (0, 0, 0).
     *
     * @return An absolute batch of this batch at the origin
     */
    @NotNull
    public AbsoluteBlockBatch toAbsoluteBatch() {
        return toAbsoluteBatch(0, 0, 0);
    }

    /**
     * Converts this batch to an absolute batch at the given coordinates.
     *
     * @param x The x position of the batch in the world
     * @param y The y position of the batch in the world
     * @param z The z position of the batch in the world
     * @return An absolute batch of this batch at (x, y, z)
     */
    @NotNull
    public AbsoluteBlockBatch toAbsoluteBatch(int x, int y, int z) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch(this.options, this.inverseOption);

        synchronized (blockIdMap) {
            for (var entry : blockIdMap.long2ObjectEntrySet()) {
                final long pos = entry.getLongKey();

                final Block block = entry.getValue();

                final int finalX = x + LocationUtils.globalBlockIndexToPositionX(pos);
                final int finalY = y + LocationUtils.globalBlockIndexToPositionY(pos);
                final int finalZ = z + LocationUtils.globalBlockIndexToPositionZ(pos);

                // this batch was made in the function, no one else has it
                batch.setBlock_UNSAFE(finalX, finalY, finalZ, block);
            }
        }
        return batch;
    }
}
