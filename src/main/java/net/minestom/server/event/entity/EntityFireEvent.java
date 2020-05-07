package net.minestom.server.event.entity;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.time.TimeUnit;

public class EntityFireEvent extends CancellableEvent {

    private int duration;
    private TimeUnit timeUnit;

    public EntityFireEvent(int duration, TimeUnit timeUnit) {
        setFireTime(duration, timeUnit);
    }

    public long getFireTime(TimeUnit timeUnit) {
        switch (timeUnit) {
            case TICK:
                return duration;
            case MILLISECOND:
                return timeUnit.toMilliseconds(duration);
            default:
                // Unexpected
                return -1;
        }
    }

    public void setFireTime(int duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

}
