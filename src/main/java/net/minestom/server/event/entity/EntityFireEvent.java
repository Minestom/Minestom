package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.utils.time.TimeUnit;

public class EntityFireEvent extends EntityEvent implements CancellableEvent {

    private int duration;

    private boolean cancelled;

    public EntityFireEvent(Entity entity, int duration, TimeUnit timeUnit) {
        super(entity);
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
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
