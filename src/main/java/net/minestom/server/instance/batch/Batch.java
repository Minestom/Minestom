package net.minestom.server.instance.batch;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.BlockModifier;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.Rotation;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;

/**
 * A Batch is a tool used to cache a list of block changes, and apply the changes whenever you want.
 * <p>
 * Batches offer a performance benefit because clients are not notified of any change until all of
 * the blocks have been placed, and because changes can happen with less synchronization.
 * <p>
 * All batches may be rotated using {link}, however rotate operations do not mutate the batch, so the
 * result should be cached if used multiple times.
 * <p>
 * If reversal is a desired behavior, batches may be applied in "reversal mode" using {link}. This
 * operation will return a new batch with the blocks set to whatever they were before the batch was
 * applied.
 *
 * @see ChunkBatch
 * @see AbsoluteBlockBatch
 * @see RelativeBlockBatch
 */
public interface Batch<Callback> extends BlockModifier {

    ExecutorService BLOCK_BATCH_POOL = new MinestomThread(MinecraftServer.THREAD_COUNT_BLOCK_BATCH, MinecraftServer.THREAD_NAME_BLOCK_BATCH);

    @Override
    default void setBlockStateId(int x, int y, int z, short blockStateId, @Nullable Data data) {
        setSeparateBlocks(x, y, z, blockStateId, (short) 0, data);
    }

    @Override
    default void setCustomBlock(int x, int y, int z, short customBlockId, @Nullable Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block with the id " + customBlockId + " does not exist!");
        setSeparateBlocks((byte) x, y, (byte) z, customBlock.getDefaultBlockStateId(), customBlockId, data);
    }

    @Override
    void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data);

    /**
     * Gets if the batch is ready to be applied to an instance.
     *
     * @return true if the batch is ready to apply
     */
    default boolean isReady() { return true; }

    /**
     * Blocks the current thread until the batch is ready to be applied.
     *
     * @see #isReady() for a non-blocking way to determine if the batch is ready
     */
    default void awaitReady() {}

    /**
     * Removes all block data from this batch.
     */
    void clear();

    /**
     * Creates a new batch with the blocks currently inside this batch,
     * rotated the given amount of degrees.
     * <p>
     * At the moment, only rotations of 90 degree increments may be applied.
     * All rotations will be rounded down to the nearest 90 degree increment.
     *
     * @param degrees The number of degrees to rotate
     * @return A new batch with the rotation applied
     *
     * @see #squareRotate(int)
     */
    default Batch<Callback> rotate(int degrees) {
        return squareRotate((degrees / 90) % 4);
    }

    /**
     * Creates a new batch with the blocks currently inside this batch,
     * rotated the given {@link Rotation}.
     * <p>
     * At the moment, only rotations of 90 degree increments may be applied.
     * 45 degree rotations will have undefined behavior.
     *
     * @param rotation The rotation to apply
     * @return A new batch with the rotation applied
     */
    default Batch<Callback> rotate(Rotation rotation) {
        return squareRotate(rotation.ordinal() / 2);
    }

    /**
     * Creates a new batch with the blocks currently inside this batch,
     * rotated the given 'degree'.
     * <p>
     * The 'degree' is the number of 90 degree rotations to apply. For
     * example: degree of 1 = 90°, 2 = 180°, -2 = -180°
     *
     * @param degree The degree to rotate
     * @return A new batch with the rotation applied
     */
    Batch<Callback> squareRotate(int degree);

    /**
     * Called to apply the batch to the given instance.
     * <p>
     * The implementation for all current batches executes the block updates in a dedicated pool,
     * and runs the callback on the next instance update after block placement is complete. This
     * means that the callback can be called up to 50ms after the blocks have been placed, however,
     * it will be called in a determinable thread. If immediate execution of the callback is needed,
     * see the unsafeApply method in each implementation.
     * <p>
     * See the specific batch classes for alternative application methods.
     *
     * @param instance The instance in which the batch should be applied
     * @param callback The callback to be executed when the batch is applied
     * @return The inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @Nullable
    Batch<Callback> apply(@NotNull Instance instance, @Nullable Callback callback);
}
