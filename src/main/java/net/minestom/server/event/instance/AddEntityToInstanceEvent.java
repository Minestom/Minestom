package net.minestom.server.event.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called by an Instance when an entity is added to it.
 * Can be used attach data.
 */
public class AddEntityToInstanceEvent implements EntityInstanceEvent, CancellableEvent {

    private final Instance instance;
    private final Entity entity;
    private Pos spawnPosition;

    private boolean cancelled;

    public AddEntityToInstanceEvent(Instance instance, Entity entity, Pos spawnPosition) {
        this.instance = instance;
        this.entity = entity;
        this.spawnPosition = spawnPosition;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    /**
     * Entity being added.
     *
     * @return the entity being added
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the position where the entity will spawn in the new instance.
     * This is the location specified in {@link Entity#setInstance(Instance, Pos)} unless modified.
     * @return the position
     */
    public Pos getSpawnPosition() {
        return spawnPosition;
    }

    /**
     * Sets the position where the entity will spawn in the new instance.
     * This can be used to override the location specified in {@link Entity#setInstance(Instance, Pos)}.
     * @param position the new spawning position
     */
    public void setSpawnPosition(Pos position) {
        this.spawnPosition = position;
    }

    /**
     * Sets the position where the entity will spawn in the new instance.
     * This can be used to override the location specified in {@link Entity#setInstance(Instance, Pos)}.
     * @param position the new spawning position
     */
    public void setSpawnPosition(Point position) {
        this.spawnPosition = position.asPos();
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
