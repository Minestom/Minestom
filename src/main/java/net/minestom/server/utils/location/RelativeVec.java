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
 */
public final class RelativeVec {

    private final Vec vec;
    private boolean relativeX, relativeY, relativeZ;
    private boolean[] relative;
    private boolean isLocal;

    public RelativeVec(@NotNull Vec vec, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this.vec = vec;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    public RelativeVec(Vec vec, boolean[] relative, boolean isLocal) {
        this.vec = vec;
        this.relative = relative;
        this.isLocal = isLocal;
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
        return isLocal ? toGlobal(vec, entity.getPosition(), relative) : toGlobal(vec, entity.getPosition().asVec(), relative);
//        final var entityPosition = entity != null ? entity.getPosition() : Pos.ZERO;
//        return from(entityPosition);
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

    public static Vec toGlobal(Vec local, Pos origin, boolean[] axis) {
        double double5 = Math.cos(Math.toRadians(origin.yaw() + 90.0f));
        double double6 = Math.sin(Math.toRadians(origin.yaw() + 90.0f));
        double double7 = Math.cos(Math.toRadians(-origin.pitch()));
        double double8 = Math.sin(Math.toRadians(-origin.pitch()));
        double double9 = Math.cos(Math.toRadians(-origin.pitch() + 90.0f));
        double double10 = Math.sin(Math.toRadians(-origin.pitch() + 90.0f));
        Vec dna11 = new Vec(double5 * double7, double8, double6 * double7);
        Vec dna12 = new Vec(double5 * double9, double10, double6 * double9);
        Vec dna13 = dna11.cross(dna12).mul(-1);
        double double14 = dna11.x() * local.z() + dna12.x() * local.y() + dna13.x() * local.x();
        double double16 = dna11.y() * local.z() + dna12.y() * local.y() + dna13.y() * local.x();
        double double18 = dna11.z() * local.z() + dna12.z() * local.y() + dna13.z() * local.x();
        return new Vec(double14 + (axis[0] ? origin.x() : 0), double16 + (axis[1] ? origin.y() : 0), double18 + (axis[2] ? origin.z() : 0));
    }

    public static Vec toGlobal(Vec relative, Vec origin, boolean[] axis) {
        if (!axis[0] && !axis[1] && !axis[2]) {
            return relative;
        }
        final var absolute = Objects.requireNonNullElse(origin, Vec.ZERO);
        final double x = relative.x() + (axis[0] ? absolute.x() : 0);
        final double y = relative.y() + (axis[1] ? absolute.y() : 0);
        final double z = relative.z() + (axis[2] ? absolute.z() : 0);
        return new Vec(x, y, z);
    }
}
