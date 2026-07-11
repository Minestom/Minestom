package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an event related to a {@link Block} happening in an {@link Instance}.
 */
public interface BlockEvent extends InstanceEvent {
    Block getBlock();

    BlockVec getBlockPosition();
}
