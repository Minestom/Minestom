package net.minestom.server.event.player;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockBreakEvent extends CancellableEvent {

    private BlockPosition blockPosition;

    private short resultBlockId;
    private short resultCustomBlockId;

    public PlayerBlockBreakEvent(BlockPosition blockPosition, short resultBlockId, short resultCustomBlockId) {
        this.blockPosition = blockPosition;
        this.resultBlockId = resultBlockId;
        this.resultCustomBlockId = resultCustomBlockId;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public short getResultBlockId() {
        return resultBlockId;
    }

    public void setResultBlockId(short resultBlockId) {
        this.resultBlockId = resultBlockId;
    }

    public short getResultCustomBlockId() {
        return resultCustomBlockId;
    }

    public void setResultCustomBlockId(short resultCustomBlockId) {
        this.resultCustomBlockId = resultCustomBlockId;
    }

    public boolean isResultCustomBlock() {
        return resultCustomBlockId != 0;
    }
}
