package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called when an instance is unregistered
 */
public class InstanceUnregisterEvent implements InstanceEvent {
    private final Instance instance;

    public InstanceUnregisterEvent(Instance instance) {
        this.instance = instance;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
