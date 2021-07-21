package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface BlockEvent extends Event {
    @NotNull Block getBlock();
}
