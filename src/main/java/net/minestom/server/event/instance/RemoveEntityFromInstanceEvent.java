package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called by an Instance when an entity is removed from it.
 */
public class RemoveEntityFromInstanceEvent extends CancellableEvent {

    private final Instance instance;
    private final Entity entity;

    public RemoveEntityFromInstanceEvent(@NotNull Instance instance, @NotNull Entity entity) {
        this.instance = instance;
        this.entity = entity;
    }

    /**
     * Gets the entity being removed.
     *
     * @return entity being removed
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Instance from which the entity is being removed.
     *
     * @return instance from which the entity is being removed
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }
}
