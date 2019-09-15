package fr.themode.minestom.event;

import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.BlockPosition;

public class PlayerStartDiggingEvent extends CancellableEvent {

    private BlockPosition blockPosition;
    private CustomBlock customBlock;

    public PlayerStartDiggingEvent(BlockPosition blockPosition, CustomBlock customBlock) {
        this.blockPosition = blockPosition;
        this.customBlock = customBlock;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public CustomBlock getBlock() {
        return customBlock;
    }
}
