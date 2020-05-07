package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockInteractEvent extends CancellableEvent {

    private BlockPosition blockPosition;
    private Player.Hand hand;

    /**
     * Does this interaction block the normal item use?
     * True for containers which open an inventory instead of letting blocks be placed
     */
    private boolean blocksItemUse;

    public PlayerBlockInteractEvent(BlockPosition blockPosition, Player.Hand hand) {
        this.blockPosition = blockPosition;
        this.hand = hand;
    }

    public boolean isBlockingItemUse() {
        return blocksItemUse;
    }

    public void setBlockingItemUse(boolean blocks) {
        this.blocksItemUse = blocks;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public Player.Hand getHand() {
        return hand;
    }
}
