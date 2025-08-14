package net.minestom.server.event.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/**
 * Called when a block in an instance is updated.
 * <p>
 * This event is triggered when a block's state changes from its instance.
 * If you wish to listen to all block updates, must be used in conjunction with {@link InstanceSectionInvalidateEvent}
 */
public class InstanceBlockUpdateEvent implements InstanceEvent, BlockEvent {
    private final Instance instance;
    private final BlockVec blockPosition;
    private final Block block;

    public InstanceBlockUpdateEvent(Instance instance, BlockVec blockPosition, Block block) {
        this.instance = instance;
        this.blockPosition = blockPosition;
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
