package net.minestom.server.event.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a block in an instance is updated.
 * <p>
 * This event is triggered when a block's state changes from its instance.
 */
public class InstanceBlockUpdateEvent implements InstanceEvent, BlockEvent {
    private final Instance instance;
    private final BlockVec blockPosition;
    private final Block block;

    public InstanceBlockUpdateEvent(@NotNull Instance instance, @NotNull BlockVec blockPosition, @NotNull Block block) {
        this.instance = instance;
        this.blockPosition = blockPosition;
        this.block = block;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
