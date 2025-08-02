package net.minestom.server.event.trait;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

/**
 * Represents an {@link EntityEvent} which happen in {@link Entity#getInstance()}.
 * Useful if you need to listen to entity events happening in its instance.
 * <p>
 * Be aware that the entity's instance must be non-null.
 */
public interface EntityInstanceEvent extends EntityEvent, InstanceEvent {
    @Override
    default Instance getInstance() {
        final Instance instance = getEntity().getInstance();
        assert instance != null : "EntityInstanceEvent is only supported on events where the entity's instance is non-null!";
        return instance;
    }
}
