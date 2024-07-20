package net.minestom.server.event.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a block is set on a chunk. {@link BlockSetEvent#getBlock()} to see what the new block is. The new block
 * may or may not differ from the old block.
 */
public class BlockSetEvent implements InstanceEvent, BlockEvent {
    private final Instance instance;
    private final Block block;
    private final BlockVec blockPosition;

    public BlockSetEvent(@NotNull Instance instance, @NotNull Block block, @NotNull Point blockPosition) {
        this.instance = instance;
        this.block = block;
        this.blockPosition = new BlockVec(blockPosition);
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
