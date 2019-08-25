package fr.themode.minestom.event;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.BlockPosition;

public class PlayerBlockPlaceEvent extends CancellableEvent {

    private short blockId;
    private BlockPosition blockPosition;
    private Player.Hand hand;

    public PlayerBlockPlaceEvent(short blockId, BlockPosition blockPosition, Player.Hand hand) {
        this.blockId = blockId;
        this.blockPosition = blockPosition;
        this.hand = hand;
    }

    public short getBlockId() {
        return blockId;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public Player.Hand getHand() {
        return hand;
    }
}
