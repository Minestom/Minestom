package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

public record EntitySetFireEvent(@NotNull Entity entity, int fireTicks, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntitySetFireEvent> {
    public EntitySetFireEvent(@NotNull Entity entity, int fireTicks) {
        this(entity, fireTicks, false);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<EntitySetFireEvent> {

        private final Entity entity;
        private int fireTicks;

        private boolean cancelled;

        public Mutator(EntitySetFireEvent event) {
            this.entity = event.entity;
            this.fireTicks = event.fireTicks;
            this.cancelled = event.cancelled;
        }

        public int getFireTicks() {
            return fireTicks;
        }

        public void setFireTicks(int fireTicks) {
            this.fireTicks = fireTicks;
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
        public @NotNull EntitySetFireEvent mutated() {
            return new EntitySetFireEvent(this.entity, this.fireTicks, this.cancelled);
        }
    }
}
