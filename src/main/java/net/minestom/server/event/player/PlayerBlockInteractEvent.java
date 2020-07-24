package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.BlockPosition;

/**
 * Called when a player interacts with a block (right-click)
 * This is also called when a block is placed
 */
public class PlayerBlockInteractEvent extends CancellableEvent {

    private final Player player;
    private BlockPosition blockPosition;
    private Player.Hand hand;
    private final BlockFace blockFace;

    /**
     * Does this interaction block the normal item use?
     * True for containers which open an inventory instead of letting blocks be placed
     */
    private boolean blocksItemUse;

    public PlayerBlockInteractEvent(Player player,
                                    BlockPosition blockPosition, Player.Hand hand, BlockFace blockFace) {
        this.player = player;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.blockFace = blockFace;
    }

    /**
     * Get the player who interacted with the block
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get if the event should block the item use
     *
     * @return true if the item use is blocked, false otherwise
     */
    public boolean isBlockingItemUse() {
        return blocksItemUse;
    }

    public void setBlockingItemUse(boolean blocks) {
        this.blocksItemUse = blocks;
    }

    /**
     * Get the position of the interacted block
     *
     * @return the block position
     */
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get the hand used for the interaction
     *
     * @return the hand used
     */
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Get the block face
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }
}
