package net.minestom.server.utils.location;

import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a location which can have fields relative to an {@link Entity} position.
 *
 * @param <T> the location type
 */
public final class RelativeVec {

    private final Vec vec;
    private boolean relativeX, relativeY, relativeZ;

    public RelativeVec(@NotNull Vec vec, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this.vec = vec;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    /**
     * Gets the location based on the relative fields and {@code position}.
     *
     * @param point the relative position
     * @return the location
     */
    public @NotNull Vec from(@Nullable Point point) {
        if (!relativeX && !relativeY && !relativeZ) {
            return vec;
        }
        final var absolute = Objects.requireNonNullElse(point, Vec.ZERO);
        final double x = vec.x() + (relativeX ? absolute.x() : 0);
        final double y = vec.y() + (relativeY ? absolute.y() : 0);
        final double z = vec.z() + (relativeZ ? absolute.z() : 0);
        return new Vec(x, y, z);
    }

    @ApiStatus.Experimental
    public Vec fromView(@Nullable Pos point) {
        if (!relativeX && !relativeY && !relativeZ) {
            return vec;
        }
        final var absolute = Objects.requireNonNullElse(point, Pos.ZERO);
        final double x = vec.x() + (relativeX ? absolute.yaw() : 0);
        final double z = vec.z() + (relativeZ ? absolute.pitch() : 0);
        return new Vec(x, 0, z);
    }

    /**
     * Gets the location based on the relative fields and {@code entity}.
     *
     * @param entity the entity to get the relative position from
     * @return the location
     */
    public @NotNull Vec from(@Nullable Entity entity) {
        final var entityPosition = entity != null ? entity.getPosition() : Pos.ZERO;
        return from(entityPosition);
    }

    public @NotNull Vec fromSender(@Nullable CommandSender sender) {
        final var entityPosition = sender instanceof Player ? ((Player) sender).getPosition() : Pos.ZERO;
        return from(entityPosition);
    }

    @ApiStatus.Experimental
    public @NotNull Vec fromView(@Nullable Entity entity) {
        final var entityPosition = entity != null ? entity.getPosition() : Pos.ZERO;
        return fromView(entityPosition);
    }

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
