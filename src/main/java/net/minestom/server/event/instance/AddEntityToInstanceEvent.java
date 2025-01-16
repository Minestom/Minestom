package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called by an Instance when an entity is added to it.
 * Can be used attach data.
 */
public record AddEntityToInstanceEvent(@NotNull Instance instance, @NotNull Entity entity, boolean cancelled) implements InstanceEvent, EntityEvent, CancellableEvent<AddEntityToInstanceEvent> {

    public AddEntityToInstanceEvent(@NotNull Instance instance, @NotNull Entity entity) {
        this(instance, entity, false);
    }

    /**
     * Entity being added.
     *
     * @return the entity being added
     */
    @NotNull
    public Entity entity() {
        return entity;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<AddEntityToInstanceEvent> {
        public Mutator(@NotNull AddEntityToInstanceEvent event) {
            super(event);
        }

        @Override
        public @NotNull AddEntityToInstanceEvent mutated() {
            return new AddEntityToInstanceEvent(this.event.instance, this.event.entity, this.isCancelled());
        }
    }
}
