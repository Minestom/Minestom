package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * This event is triggered when a section of an instance is manually marked as invalid.
 * <p>
 * Changes in this case are not known but indicate that its content must be reinterpreted.
 */
public class InstanceSectionInvalidateEvent implements InstanceEvent {
    private final Instance instance;
    private final int sectionX, sectionY, sectionZ;

    public InstanceSectionInvalidateEvent(Instance instance, int sectionX, int sectionY, int sectionZ) {
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
