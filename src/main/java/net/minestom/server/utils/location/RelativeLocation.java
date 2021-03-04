package net.minestom.server.utils.location;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a location which can have fields relative to an {@link Entity} position.
 *
 * @param <T> the location type
 */
public abstract class RelativeLocation<T> {

    protected T location;
    protected boolean relativeX, relativeY, relativeZ;

    public RelativeLocation(@NotNull T location, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this.location = location;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    /**
     * Gets the location based on the relative fields and {@code entity}.
     *
     * @param entity the entity to get the relative position from
     * @return the location
     */
    public T from(@Nullable Entity entity) {

        final Position entityPosition = entity != null ? entity.getPosition() : new Position();

        return from(entityPosition);
    }

    /**
     * Gets the location based on the relative fields and {@code position}.
     *
     * @param position the relative position
     * @return the location
     */
    public abstract T from(@Nullable Position position);

    /**
     * Gets if the 'x' field is relative.
     *
     * @return true if the 'x' field is relative
     */
    public boolean isRelativeX() {
        return relativeX;
    }

    /**
     * Gets if the 'y' field is relative.
     *
     * @return true if the 'y' field is relative
     */
    public boolean isRelativeY() {
        return relativeY;
    }

    /**
     * Gets if the 'z' field is relative.
     *
     * @return true if the 'z' field is relative
     */
    public boolean isRelativeZ() {
        return relativeZ;
    }
}
