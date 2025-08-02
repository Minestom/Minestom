package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called when an instance is registered
 */
public class InstanceRegisterEvent implements InstanceEvent {
    private final Instance instance;

    public InstanceRegisterEvent(Instance instance) {
        this.instance = instance;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
