package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an instance is unregistered
 */
public class InstanceUnregisterEvent implements InstanceEvent {
    private final Instance instance;

    public InstanceUnregisterEvent(@NotNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
