package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} hits a block (left-click).
 * <p>
 * It's called right before {@link PlayerStartDiggingEvent} or {@link PlayerBlockBreakEvent}
 * if the player is in Creative mode.
 */
public class PlayerBlockHitEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final Block block;
    private final Point blockPosition;
    private final BlockFace blockFace;

    private boolean cancelled;

    public PlayerBlockHitEvent(@NotNull Player player, @NotNull Block block, @NotNull Point blockPosition,
                               @NotNull BlockFace blockFace) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    /**
     * Gets the block which is being hit.
     *
     * @return the block
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    public @NotNull Point getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the face you are hitting
     *
     * @return the block face
     */
    public @NotNull BlockFace getBlockFace() {
        return blockFace;
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
