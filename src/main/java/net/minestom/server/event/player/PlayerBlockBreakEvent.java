package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockBreakEvent extends PlayerEvent implements CancellableEvent {

    private final Block block;
    private Block resultBlock;
    private final BlockPosition blockPosition;

    private boolean cancelled;

    public PlayerBlockBreakEvent(@NotNull Player player,
                                 @NotNull Block block, @NotNull Block resultBlock, @NotNull BlockPosition blockPosition) {
        super(player);
        this.block = block;
        this.resultBlock = resultBlock;
        this.blockPosition = blockPosition;
    }

    /**
     * Gets the block to break
     *
     * @return the block
     */
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
    public @NotNull BlockPosition getBlockPosition() {
        return blockPosition;
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
