package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class EntityFireEvent extends Event implements EntityEvent, CancellableEvent {

    private final Entity entity;
    private int duration;
    private TimeUnit timeUnit;

    private boolean cancelled;

    public EntityFireEvent(Entity entity, int duration, TimeUnit timeUnit) {
        this.entity = entity;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
}
