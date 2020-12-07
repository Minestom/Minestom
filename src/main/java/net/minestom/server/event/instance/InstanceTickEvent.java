package net.minestom.server.event.instance;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an instance processes a tick.
 */
public class InstanceTickEvent extends Event {

    private final int duration;
    private final Instance instance;

    public InstanceTickEvent(@NotNull Instance instance, long time, long lastTickAge) {
        this.duration = (int) (time - lastTickAge);
        this.instance = instance;
    }

    /**
     * Gets the duration of the tick in ms.
     *
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Gets the instance of the event.
     *
     * @return the instance
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }
}