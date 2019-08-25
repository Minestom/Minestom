package fr.themode.minestom.event;

import fr.themode.minestom.utils.BlockPosition;

public class PlayerBlockBreakEvent extends CancellableEvent {

    private BlockPosition blockPosition;

    public PlayerBlockBreakEvent(BlockPosition blockPosition) {
        this.blockPosition = blockPosition;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }
}
