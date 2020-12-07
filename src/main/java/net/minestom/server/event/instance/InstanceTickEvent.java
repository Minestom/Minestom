package net.minestom.server.event.instance;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.event.Event;

/**
 * Called when an instance processes a tick 
 */
public class InstanceTickEvent extends Event {

    private final int duration;

    public InstanceTickEvent(@NotNull long time, @Nullable long lastTickAge) {
        this.duration = (int) (time - lastTickAge);
    }

    /**
     * Gets the duration of the tick in ms
     *
     * @return the instance
     */
    @NotNull
    public int getDuration() {
        return duration;
    }
}