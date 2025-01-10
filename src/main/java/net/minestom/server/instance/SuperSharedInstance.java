package net.minestom.server.instance;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * SuperSharedInstance is an instance that acts exactly like a {@link SharedInstance} in that chunks are shared and entities
 * are separated. However, entities within the underlying {@link InstanceContainer} will be shown in this instance.
 */
public class SuperSharedInstance extends SharedInstance {
    public SuperSharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer);
    }
}
