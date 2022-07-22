package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class InstanceRegisteredEvent implements InstanceEvent {

    private final Instance instance;

    public InstanceRegisteredEvent(final @NotNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull Instance getInstance() {
        return this.instance;
    }
}
