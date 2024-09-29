package net.minestom.server.instance.batch;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * A Batch is a tool used to cache a list of block changes, and apply the changes whenever you want.
 * <p>
 * Batches offer a performance benefit because clients are not notified of any change until all
 * the blocks have been placed, and because changes can happen with less synchronization.
 * <p>
 * If reversal is a desired behavior, batches may be applied in "reversal mode" using {@link BatchOption}.
 * This operation will return a new batch with the blocks set to whatever they were before the batch was applied.
 *
 * @see ChunkBatch
 * @see AbsoluteBlockBatch
 * @see RelativeBlockBatch
 */
public interface Batch extends Block.Setter {
    /**
     * Removes all block data from this batch.
     */
    void clear();

    /**
     * Called to apply the batch to the given instance.
     * See the specific batch classes for alternative application methods.
     *
     * @param instance The instance in which the batch should be applied
     * @return A completable future for the inverse of this batch, if inverse is enabled in the {@link BatchOption}
     */
    @NotNull
    CompletableFuture<? extends @Nullable Batch> apply(@NotNull Instance instance);
}
