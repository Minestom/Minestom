package net.minestom.server.event;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockPlaceEvent extends CancellableEvent {

    private short blockId;
    private BlockPosition blockPosition;
    private Player.Hand hand;

    private boolean consumeBlock;

    public PlayerBlockPlaceEvent(short blockId, BlockPosition blockPosition, Player.Hand hand) {
        this.blockId = blockId;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
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

    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    public boolean doesConsumeBlock() {
        return consumeBlock;
    }
}
