package net.minestom.server.event.instance;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Called when an instance processes a tick 
 */
public class InstanceTickEvent extends Event {

	private final int duration;
	private final Instance instance;

    public InstanceTickEvent(long time, long lastTickAge, Instance someInstance) {
        this.duration = (int) (time - lastTickAge);
        this.instance = someInstance;
    }

    /**
     * Gets the duration of the tick in ms
     *
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Gets the instance of the event
     *
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }
}