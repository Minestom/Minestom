package net.minestom.server.event;

import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

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
