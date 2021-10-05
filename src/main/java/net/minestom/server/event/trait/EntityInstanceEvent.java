package net.minestom.server.event.trait;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link EntityEvent} which happen in {@link Entity#getInstance()}.
 */
@ApiStatus.Internal
public interface EntityInstanceEvent extends EntityEvent, InstanceEvent {
    @Override
    default @NotNull Instance getInstance() {
        return getEntity().getInstance();
    }
}
