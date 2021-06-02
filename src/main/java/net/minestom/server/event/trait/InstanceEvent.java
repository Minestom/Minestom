package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface InstanceEvent extends Event {

    /**
     * Gets the instance.
     *
     * @return instance
     */
    @NotNull Instance getInstance();
}