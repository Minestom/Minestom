package net.minestom.server.event.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents after the block was set
 */
@SuppressWarnings("unused")
public final class PostSetBlockEvent implements Event, BlockEvent, CancellableEvent {

    private boolean cancelled = false;

    private final Block newBlock;
    private final Block previousBlock;
    private final Instance instance;
    private final BlockFace blockFace;
    private final BlockVec position;
    private final BlockEvent.Source source;

    public PostSetBlockEvent(
        @NotNull Block newBlock,
        @NotNull Block previousBlock,
        @NotNull Instance instance,
        @Nullable BlockFace face,
        @NotNull BlockVec position,
        @NotNull BlockEvent.Source source
    ) {
        this.newBlock = newBlock;
        this.previousBlock = previousBlock;
        this.instance = instance;
        this.blockFace = face;
        this.position = position;
        this.source = source;
    }

    /**
     * Gets the block which replaced {@link #getPreviousBlock()}
     *
     * @return the result block
     */
    @Override
    public @NotNull Block getBlock() {
        return newBlock;
    }

    /**
     * Gets the changed block
     *
     * @return the block
     */
    public @NotNull Block getPreviousBlock() {
        return previousBlock;
    }

    /**
     * Gets instance where the block was changed
     *
     * @return the instance
     */
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * Gets the face at which the block was changed
     *
     * @return the block face
     */
    public @Nullable BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    /**
     * Gets the {@link BlockEvent.Source}
     *
     * @return the Events Source
     */
    public @NotNull BlockEvent.Source getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
