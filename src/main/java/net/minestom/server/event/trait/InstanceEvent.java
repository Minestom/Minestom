package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Represents any event targeting an {@link Instance}.
 */
public interface InstanceEvent extends Event {

    /**
     * Gets the instance.
     *
     * @return instance
     */
    Instance getInstance();
}
