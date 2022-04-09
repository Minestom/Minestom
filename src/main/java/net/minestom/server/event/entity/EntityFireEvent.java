package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public class EntityFireEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private Duration duration;

    private boolean cancelled;

    public EntityFireEvent(Entity entity, int duration, TemporalUnit temporalUnit) {
        this(entity, Duration.of(duration, temporalUnit));
    }

    public EntityFireEvent(Entity entity, Duration duration) {
        this.entity = entity;
        setFireTime(duration);
    }

    public long getFireTime(TemporalUnit temporalUnit) {
        return duration.toNanos() / temporalUnit.getDuration().toNanos();
    }

    public void setFireTime(int duration, TemporalUnit temporalUnit) {
        setFireTime(Duration.of(duration, temporalUnit));
    }

    public void setFireTime(Duration duration) {
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

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
}
