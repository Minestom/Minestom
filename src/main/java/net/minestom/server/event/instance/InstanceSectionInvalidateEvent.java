package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Signal an instance section content has been invalidated.
 */
public class InstanceSectionInvalidateEvent implements InstanceEvent {
    private final Instance instance;
    private final int sectionX, sectionY, sectionZ;

    public InstanceSectionInvalidateEvent(@NotNull Instance instance, int sectionX, int sectionY, int sectionZ) {
        this.instance = instance;
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }

    public int sectionX() {
        return sectionX;
    }

    public int sectionY() {
        return sectionY;
    }

    public int sectionZ() {
        return sectionZ;
    }
}
