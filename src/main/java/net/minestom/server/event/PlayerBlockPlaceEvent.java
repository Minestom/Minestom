package net.minestom.server.event;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockPlaceEvent extends CancellableEvent {

    private final Player player;
    private short blockId;
    private short customBlockId;
    private BlockPosition blockPosition;
    private Player.Hand hand;

    private boolean consumeBlock;

    public PlayerBlockPlaceEvent(Player player, short blockId, short customBlockId, BlockPosition blockPosition, Player.Hand hand) {
        this.player = player;
        this.blockId = blockId;
        this.customBlockId = customBlockId;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
    }

    public void setCustomBlockId(short customBlockId) {
        this.customBlockId = customBlockId;
    }

    public short getCustomBlockId() {
        return customBlockId;
    }

    public void setBlockId(short blockId) {
        this.blockId = blockId;
    }

    public short getBlockId() {
        return blockId;
    }

    public Player getPlayer() {
        return player;
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
