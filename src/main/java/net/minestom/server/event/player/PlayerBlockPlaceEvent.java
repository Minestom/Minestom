package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockPlaceEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private Block block;
    private final BlockFace blockFace;
    private final Point blockPosition;
    private final Player.Hand hand;

    private boolean consumeBlock;

    private boolean cancelled;

    public PlayerBlockPlaceEvent(@NotNull Player player, @NotNull Block block,
                                 @NotNull BlockFace blockFace,
                                 @NotNull Point blockPosition, @NotNull Player.Hand hand) {
        this.player = player;
        this.block = block;
        this.blockFace = blockFace;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Changes the block to be placed.
     *
     * @param block the new block
     */
    public void setBlock(@NotNull Block block) {
        this.block = block;
    }

    public @NotNull BlockFace getBlockFace() {
        return blockFace;
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
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    public @NotNull Player.Hand getHand() {
        return hand;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        return consumeBlock;
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
