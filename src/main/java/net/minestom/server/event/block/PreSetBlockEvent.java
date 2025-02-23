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
 * Represents before the block was set
 */
@SuppressWarnings("unused")
public final class PreSetBlockEvent implements Event, BlockEvent, CancellableEvent {

    private boolean cancelled = false;

    private Block newBlock;

    private boolean doBlockUpdates = true;
    private boolean doesConsumeBlock = true;

    private final Block previousBlock;
    private final Instance instance;
    private final BlockFace blockFace;
    private final BlockVec position;
    private final BlockEvent.Source source;

    public PreSetBlockEvent(
        @NotNull Block newBlock,
        @NotNull Block previousBlock,
        @NotNull Instance instance,
        @Nullable BlockFace blockFace,
        @NotNull BlockVec position,
        @NotNull BlockEvent.Source source
    ) {
        this.newBlock = newBlock;
        this.previousBlock = previousBlock;
        this.instance = instance;
        this.blockFace = blockFace;
        this.position = position;
        this.source = source;
    }

    /**
     * Gets the block which will replace {@link #getPreviousBlock()}
     *
     * @return the result block
     */
    @Override
    public @NotNull Block getBlock() {
        return newBlock;
    }

    /**
     * Gets the block which will be changed
     *
     * @return the block
     */
    public @NotNull Block getPreviousBlock() {
        return previousBlock;
    }

    /**
     * Gets instance where the block is being changed
     *
     * @return the instance
     */
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * Gets the face at which the block is being changed
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

    /**
     * Should the place trigger updates (on self and neighbors)
     * @return true if this placement should do block updates
     */
    public boolean doBlockUpdates() {
        return doBlockUpdates;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean consumesBlock() {
        return doesConsumeBlock;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @param doesConsumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void setDoesConsumeBlock(boolean doesConsumeBlock) {
        this.doesConsumeBlock = doesConsumeBlock;
    }

    /**
     * Should the place trigger updates (on self and neighbors)
     * @param doBlockUpdates true if this placement should do block updates
     */
    public void setDoBlockUpdates(boolean doBlockUpdates) {
        this.doBlockUpdates = doBlockUpdates;
    }

    /**
     * Changes the block to be placed.
     *
     * @param block the new block
     */
    public void setBlock(@NotNull Block block) {
        this.newBlock = block;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}