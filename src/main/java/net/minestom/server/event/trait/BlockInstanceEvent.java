package net.minestom.server.event.trait;

import net.minestom.server.instance.Instance;

/**
 * Represents a {@link BlockEvent} happening in an {@link Instance}.
 * Most block events should use this interface, unless they don't have an explicit instance.
 */
public interface BlockInstanceEvent extends BlockEvent, InstanceEvent {
}
