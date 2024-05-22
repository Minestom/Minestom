package net.minestom.server.event.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

//Microtus start - integrate world spawn position
/**
 * The event is triggered by the server when an instance successfully changes its world spawn position.
 * By implementing a listener for this event, developers can track changes to a world's spawn position initiated by instances during runtime.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.1.3
 */
public class InstanceWorldPositionChangeEvent implements InstanceEvent {

    private final Instance instance;
    private final Pos oldPosition;

    /**
     * Constructs a new {@code InstanceWorldPositionChangeEvent} with the specified parameters.
     *
     * @param instance      the involved instance
     * @param oldPosition   the old position of the instance before the change
     */
    public InstanceWorldPositionChangeEvent(@NotNull Instance instance, @NotNull Pos oldPosition) {
        this.instance = instance;
        this.oldPosition = oldPosition;
    }

    /**
     * Gets the new position of the instance after the change.
     *
     * @return the new position
     */
    public @NotNull Pos getNewPosition() {
        return instance.getWorldSpawnPosition();
    }

    /**
     * Gets the old position of the instance before the change.
     *
     * @return the old position
     */
    public @NotNull Pos getOldPosition() {
        return oldPosition;
    }

    /**
     * Gets the instance which received a world position change.
     *
     * @return the involved instance
     */
    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
//Microtus end - integrate world spawn position