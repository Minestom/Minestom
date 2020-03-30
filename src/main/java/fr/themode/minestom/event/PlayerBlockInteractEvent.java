package fr.themode.minestom.event;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.BlockPosition;

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
