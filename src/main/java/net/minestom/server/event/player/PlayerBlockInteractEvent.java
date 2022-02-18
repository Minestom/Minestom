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
 * Called when a player interacts with a block (right-click).
 * This is also called when a block is placed.
 */
public class PlayerBlockInteractEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final Player.Hand hand;
    private final Block block;
    private final Point blockPosition;
    private final BlockFace blockFace;

    /**
     * Does this interaction block the normal item use?
     * True for containers which open an inventory instead of letting blocks be placed
     */
    private boolean blocksItemUse;

    private boolean cancelled;

    public PlayerBlockInteractEvent(@NotNull Player player, @NotNull Player.Hand hand,
                                    @NotNull Block block, @NotNull Point blockPosition,
                                    @NotNull BlockFace blockFace) {
        this.player = player;
        this.hand = hand;
        this.block = block;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    /**
     * Gets if the event should block the item use.
     *
     * @return true if the item use is blocked, false otherwise
     */
    public boolean isBlockingItemUse() {
        return blocksItemUse;
    }

    public void setBlockingItemUse(boolean blocks) {
        this.blocksItemUse = blocks;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the position of the interacted block.
     *
     * @return the block position
     */
    public @NotNull Point getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the hand used for the interaction.
     *
     * @return the hand used
     */
    public @NotNull Player.Hand getHand() {
        return hand;
    }

    /**
     * Gets the block face.
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
