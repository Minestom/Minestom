package fr.themode.minestom.event;

import fr.themode.minestom.utils.BlockPosition;

public class BlockPlaceEvent extends CancellableEvent {

    private short blockId;
    private BlockPosition blockPosition;

    public BlockPlaceEvent(short blockId, BlockPosition blockPosition) {
        this.blockId = blockId;
        this.blockPosition = blockPosition;
    }

    public short getBlockId() {
        return blockId;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }
}
