package net.minestom.server.instance.batch;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents options for {@link Batch}s.
 */
public class BatchOption {

    private boolean fullChunk = false;
    private boolean calculateInverse = false;
    private boolean unsafeApply = false;

    public BatchOption() {
    }

    /**
     * Gets if the batch is responsible for composing the whole chunk.
     * <p>
     * Having it to true means that the batch will clear the chunk data before placing the blocks.
     * <p>
     * Defaults to false.
     *
     * @return true if the batch is responsible for all the chunk
     */
    public boolean isFullChunk() {
        return fullChunk;
    }

    /**
     * Gets if the batch will calculate the inverse of the batch when it is applied for an 'undo' behavior.
     * <p>
     * This flag will determine the return value of {@link Batch#apply(Instance, Object)} (and other variants).
     * If true, a {@link Batch} will be returned. Otherwise null will be returned.
     * <p>
     * Defaults to false.
     *
     * @return true if the batch will calculate its inverse on application
     * @see #isUnsafeApply()
     */
    public boolean shouldCalculateInverse() {
        return calculateInverse;
    }

    /**
     * Gets if the batch will wait ignore whether it is ready or not when applying it.
     * <p>
     * If set, the batch may not be ready, or it may be partially ready which will cause an undefined result.
     * {@link Batch#isReady()} and {@link Batch#awaitReady()} may be used to check if it is ready and block
     * until it is ready.
     * <p>
     * The default implementations of {@link ChunkBatch}, {@link AbsoluteBlockBatch}, and {@link RelativeBlockBatch}
     * are always ready unless they are an inverse batch. This is not a safe assumption, and may change in the future.
     * <p>
     * Defaults to false.
     *
     * @return true if the batch will immediately
     */
    public boolean isUnsafeApply() {
        return this.unsafeApply;
    }

    /**
     * @param fullChunk true to make this batch composes the whole chunk
     * @return 'this' for chaining
     * @see #isFullChunk()
     */
    @NotNull
    @Contract("_ -> this")
    public BatchOption setFullChunk(boolean fullChunk) {
        this.fullChunk = fullChunk;
        return this;
    }

    /**
     * @param calculateInverse true to make this batch calculate the inverse on application
     * @return 'this' for chaining
     * @see #shouldCalculateInverse()
     */
    @NotNull
    @Contract("_ -> this")
    public BatchOption setCalculateInverse(boolean calculateInverse) {
        this.calculateInverse = calculateInverse;
        return this;
    }

    /**
     * @param unsafeApply true to make this batch apply without checking if it is ready to apply.
     * @return 'this' for chaining
     * @see #isUnsafeApply()
     * @see Batch#isReady()
     */
    @NotNull
    @Contract("_ -> this")
    public BatchOption setUnsafeApply(boolean unsafeApply) {
        this.unsafeApply = unsafeApply;
        return this;
    }
}
