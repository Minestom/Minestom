package net.minestom.server.event.instance;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceContainer;

/**
 * Called when an instance processes a tick 
 */
public class InstanceTickEvent extends Event {

	private final int duration;
	private final InstanceContainer instance;

    public InstanceTickEvent(@NotNull long time, @Nullable long lastTickAge, InstanceContainer someInstance) {
        this.duration = (int) (time - lastTickAge);
        this.instance = someInstance;
    }

    /**
     * Gets the duration of the tick in ms
     *
     * @return the duration
     */
    @NotNull
    public int getDuration() {
        return duration;
    }
    
    /**
     * Gets the instance of the event
     *
     * @return the instance
     */
    @NotNull
    public InstanceContainer getInstance() {
        return instance;
    }
}