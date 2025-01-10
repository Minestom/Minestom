package net.minestom.server.instance;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * SuperSharedInstance is an instance that acts exactly like a {@link SharedInstance} in that chunks are shared and entities
 * are separated. However, entities within the underlying {@link InstanceContainer} will be shown in this instance.
 */
public class SuperSharedInstance extends SharedInstance {
    public SuperSharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer);
        // register entities already inside of the instanceContainer
        EntityTracker entityTracker = getEntityTracker();
        for (Entity e : instanceContainer.getEntities()) {
            entityTracker.register(e, e.getPosition(), e.getTrackingTarget(), e.getTrackingUpdate());
        }
    }
}
