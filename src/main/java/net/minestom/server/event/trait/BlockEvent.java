package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an event related to a {@link Block}.
 * Because almost all block events happen in a specific instance, it is recommended to use {@link BlockInstanceEvent}.
 */
@ApiStatus.Internal
public interface BlockEvent extends Event {
    Block getBlock();

    BlockVec getBlockPosition();
}
