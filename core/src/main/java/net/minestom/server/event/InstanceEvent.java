package net.minestom.server.event;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class InstanceEvent extends Event {

    protected final Instance instance;

    public InstanceEvent(@NotNull Instance instance) {
        this.instance = instance;
    }

    /**
     * Gets the instance.
     *
     * @return instance
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }
}