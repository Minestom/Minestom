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
     * Setting it to true means that the batch will clear the chunk data before placing the blocks.
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
     * If true, a {@link Batch} will be returned.
     * Otherwise, null will be returned.
     * <p>
     * WARNING:
     * The inverse returned by {@link Batch#apply(Instance)} will revert to the cleared chunk
     * if {@link #isFullChunk()} is true.
     * This may change in the future.
     * <p>
     * Defaults to false.
     *
     * @return true if the batch will calculate its inverse on application
     */
    public boolean shouldCalculateInverse() {
        return calculateInverse;
    }

    /**
     * Gets if the batch will send the chunk to viewers on application.
     * <p>
     * Setting it to false means that viewers (players) will not see the updated blocks.
     * <p>
     * Defaults to true.
     *
     * @return true if the batch will send block updates to viewers
     */
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

    /**
     * @param sendUpdate true to make this batch send the chunk to viewers on application
     * @return 'this' for chaining
     * @see #shouldSendUpdate()
     */
    @NotNull
    @Contract("_ -> this")
    public BatchOption setSendUpdate(boolean sendUpdate) {
        this.sendUpdate = sendUpdate;
        return this;
    }
}
