package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.block.BreakBlockEvent;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link PostBreakBlockEvent} or {@link BreakBlockEvent}
 */
@Deprecated()
public class PlayerBlockBreakEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final BlockEvent.Source.Player source;
    private final Block block;
    private Block resultBlock;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;

    private boolean cancelled;

    @Deprecated()
    public PlayerBlockBreakEvent(@NotNull Player player,
                                 @NotNull Block block, @NotNull Block resultBlock, @NotNull BlockVec blockPosition,
                                 @NotNull BlockFace blockFace, @NotNull BlockEvent.Source.Player source) {
        this.player = player;

        this.block = block;
        this.resultBlock = resultBlock;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
        this.source = source;
    }

    /**
     * Gets the block to break
     *
     * @return the block
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the block which will replace {@link #getBlock()}.
     *
     * @return the result block
     */
    public @NotNull Block getResultBlock() {
        return resultBlock;
    }

    /**
     * Gets the face at which the block was broken
     *
     * @return the block face
     */
    public @NotNull BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Changes the result of the event.
     *
     * @param resultBlock the new block
     */
    public void setResultBlock(@NotNull Block resultBlock) {
        this.resultBlock = resultBlock;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the {@link BlockEvent.Source}
     *
     * @return the Events Source
     */
    @Override
    public @NotNull BlockEvent.Source.Player getSource() {
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

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
