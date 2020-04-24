package net.minestom.server.event;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockInteractEvent extends CancellableEvent {

    private BlockPosition blockPosition;
    private Player.Hand hand;

    public PlayerBlockInteractEvent(BlockPosition blockPosition, Player.Hand hand) {
        this.blockPosition = blockPosition;
        this.hand = hand;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public Player.Hand getHand() {
        return hand;
    }
}
