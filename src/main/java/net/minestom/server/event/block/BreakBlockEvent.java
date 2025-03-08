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
 * Represents before the block was broken
 */
public final class BreakBlockEvent implements Event, BlockEvent, CancellableEvent {

    private boolean cancelled = false;

    private Block newBlock = Block.AIR;

    private final Block previousBlock;
    private final Instance instance;
    private final BlockVec position;
    private final BlockEvent.Source source;

    public BreakBlockEvent(
        @NotNull Block previousBlock,
        @NotNull Instance instance,
        @NotNull BlockVec position,
        @NotNull BlockEvent.Source source
    ) {
        this.previousBlock = previousBlock;
        this.instance = instance;
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
     * Gets the broken block
     *
     * @return the block
     */
    public @NotNull Block getPreviousBlock() {
        return previousBlock;
    }

    /**
     * Gets instance where the block is being broken
     *
     * @return the instance
     */
    public @NotNull Instance getInstance() {
        return instance;
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

    public void setBlock(@NotNull Block block) {
        this.newBlock = block;
    }
}