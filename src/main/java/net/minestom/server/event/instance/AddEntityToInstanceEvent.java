package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called by an Instance when an entity is added to it.
 * Can be used attach data.
 */
public class AddEntityToInstanceEvent extends CancellableEvent {

    private final Instance instance;
    private final Entity entity;

    public AddEntityToInstanceEvent(@NotNull Instance instance, @NotNull Entity entity) {
        this.instance = instance;
        this.entity = entity;
    }

    /**
     * Entity being added.
     *
     * @return the entity being added
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Instance in which the entity is being added
     *
     * @return instance in which the entity is being added
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }
}
