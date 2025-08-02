package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.instance.block.Block;

public interface BlockEvent extends Event {
    Block getBlock();

    BlockVec getBlockPosition();
}
