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
    private boolean sendUpdate = true;

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
     * This flag will determine the return value of {@link Batch#apply(Instance)} (and other variants).
     * If true, a {@link Batch} will be returned. Otherwise null will be returned.
     * <p>
     * Defaults to false.
     *
     * @return true if the batch will calculate its inverse on application
     */
    public boolean shouldCalculateInverse() {
        return calculateInverse;
    }

    public boolean shouldSendUpdate() {
        return sendUpdate;
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

    @Contract("_ -> this")
    public BatchOption setSendUpdate(boolean sendUpdate) {
        this.sendUpdate = sendUpdate;
        return this;
    }
}
