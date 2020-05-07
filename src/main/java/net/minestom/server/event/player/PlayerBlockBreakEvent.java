package net.minestom.server.event.player;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockBreakEvent extends CancellableEvent {

    private BlockPosition blockPosition;

    private short blockId;
    private boolean customBlock;

    public PlayerBlockBreakEvent(BlockPosition blockPosition) {
        this.blockPosition = blockPosition;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public short getResultBlock() {
        return blockId;
    }

    public void setResultBlock(short blockId) {
        this.blockId = blockId;
        this.customBlock = false;
    }

    public boolean isResultCustomBlock() {
        return customBlock;
    }

    public void setResultCustomBlock(short customBlockId) {
        this.blockId = customBlockId;
        this.customBlock = true;
    }
}
