package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

public record EntityFireExtinguishEvent(Entity entity, boolean natural, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntityFireExtinguishEvent> {

    public EntityFireExtinguishEvent(Entity entity, boolean natural) {
        this(entity, natural, false);
    }

    @Override
    public @NotNull Entity entity() {
        return entity;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<EntityFireExtinguishEvent> {
        public Mutator(@NotNull EntityFireExtinguishEvent event) {
            super(event);
        }

        @Override
        public @NotNull EntityFireExtinguishEvent mutated() {
            return new EntityFireExtinguishEvent(event.entity(), event.natural(), this.isCancelled());
        }
    }
}
