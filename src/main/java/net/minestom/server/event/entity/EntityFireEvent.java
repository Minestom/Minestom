package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.utils.time.Tick;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public class EntityFireEvent implements EntityEvent, CancellableEvent {

    private final Entity entity;
    private Duration duration;

    private boolean cancelled;

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public EntityFireEvent(Entity entity, int duration, net.minestom.server.utils.time.TimeUnit timeUnit) {
        this(entity, Duration.ofMillis(timeUnit.toMilliseconds(duration)));
    }

    public EntityFireEvent(Entity entity, Duration duration) {
        this.entity = entity;
        setFireTime(duration);
    }

    /**
     * @deprecated Replaced by {@link #getFireTime(TemporalUnit)}
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public long getFireTime(net.minestom.server.utils.time.TimeUnit timeUnit) {
        switch (timeUnit) {
            case TICK:
                return duration.toMillis() / Tick.TICKS.getDuration().toMillis();
            case MILLISECOND:
                return duration.toMillis();
            default:
                // Unexpected
                return -1;
        }
    }

    public long getFireTime(TemporalUnit temporalUnit) {
        return duration.toNanos() / temporalUnit.getDuration().toNanos();
    }

    /**
     * @deprecated Replaced by {@link #setFireTime(Duration)}
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public void setFireTime(int duration, net.minestom.server.utils.time.TimeUnit timeUnit) {
        setFireTime(Duration.ofMillis(timeUnit.toMilliseconds(duration)));
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
