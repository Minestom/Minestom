package fr.themode.minestom.event;

import fr.themode.minestom.utils.BlockPosition;

public class BlockBreakEvent extends CancellableEvent {

    private BlockPosition blockPosition;

    public BlockBreakEvent(BlockPosition blockPosition) {
        this.blockPosition = blockPosition;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }
}
