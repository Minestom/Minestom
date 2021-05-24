package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockPlaceEvent extends PlayerEvent implements CancellableEvent {

    private Block block;
    private final BlockPosition blockPosition;
    private final Player.Hand hand;

    private boolean consumeBlock;

    private boolean cancelled;

    public PlayerBlockPlaceEvent(@NotNull Player player, @NotNull Block block,
                                 @NotNull BlockPosition blockPosition, @NotNull Player.Hand hand) {
        super(player);
        this.block = block;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
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

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @NotNull
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    @NotNull
    public Player.Hand getHand() {
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
}
