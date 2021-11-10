package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockBreakEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final Block block;
    private Block resultBlock;
    private final Point blockPosition;

    private boolean cancelled;

    public PlayerBlockBreakEvent(@NotNull Player player,
                                 @NotNull Block block, @NotNull Block resultBlock, @NotNull Point blockPosition) {
        this.player = player;

        this.block = block;
        this.resultBlock = resultBlock;
        this.blockPosition = blockPosition;
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
    public @NotNull Point getBlockPosition() {
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

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
