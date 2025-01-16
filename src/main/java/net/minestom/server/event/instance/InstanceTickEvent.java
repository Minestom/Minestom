package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an instance processes a tick.
 */
public record InstanceTickEvent(@NotNull Instance instance, int duration) implements InstanceEvent {

    public InstanceTickEvent(@NotNull Instance instance, long time, long lastTickAge) {
        this(instance, (int) (time - lastTickAge));
    }

    /**
     * Gets the duration of the tick in ms.
     *
     * @return the duration
     */
    @Override
    public int duration() {
        return duration;
    }
}