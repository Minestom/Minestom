package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a velocity is applied to an entity using {@link Entity#setVelocity(Vec)}.
 */
public record EntityVelocityEvent(@NotNull Entity entity, @NotNull Vec velocity, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntityVelocityEvent> {

    public EntityVelocityEvent(@NotNull Entity entity, @NotNull Vec velocity) {
        this(entity, velocity, false);
    }

    /**
     * Gets the enity who will be affected by {@link #velocity()}.
     *
     * @return the entity
     */
    @Override
    public @NotNull Entity entity() {
        return entity;
    }

    /**
     * Gets the velocity which will be applied.
     *
     * @return the velocity
     */
    @Override
    public @NotNull Vec velocity() {
        return velocity;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<EntityVelocityEvent> {
        private final Entity entity;
        private Vec velocity;

        private boolean cancelled;

        public Mutator(EntityVelocityEvent event) {
            this.entity = event.entity;
            this.velocity = event.velocity;
            this.cancelled = event.cancelled;
        }

        public @NotNull Vec getVelocity() {
            return velocity;
        }

        public void setVelocity(@NotNull Vec velocity) {
            this.velocity = velocity;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public @NotNull EntityVelocityEvent mutated() {
            return new EntityVelocityEvent(this.entity, this.velocity, this.cancelled);
        }
    }
}
