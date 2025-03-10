package net.minestom.server.utils.location;

import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a location which can have fields relative to an {@link Entity} position.
 * <p>
 * Useful for parsing Vec2 or Vec3 types
 */
public record RelativeVec(@NotNull Vec vec, @NotNull CoordinateType coordinateType, boolean relativeX, boolean relativeY, boolean relativeZ) {

    public RelativeVec {
        Check.argCondition(relativeX && coordinateType == CoordinateType.ABSOLUTE, "RelativeVec `x` cannot have relativity while coordinateType is absolute.");
        Check.argCondition(relativeY && coordinateType == CoordinateType.ABSOLUTE, "RelativeVec `y` cannot have relativity while coordinateType is absolute.");
        Check.argCondition(relativeZ && coordinateType == CoordinateType.ABSOLUTE, "RelativeVec `z` cannot have relativity while coordinateType is absolute.");

        // Only XZ for Vec2 types, so we need to check y as well.
        Check.argCondition(coordinateType == CoordinateType.LOCAL && !(relativeX && (relativeY || vec.y() == 0) && relativeZ), "RelativeVec is always relative while coordinateType is local.");
    }

    /**
     * Gets the location based on the relative fields and {@link #vec()}.
     *
     * @param origin the origin position, null if none
     * @return the location
     */
    public @NotNull Vec from(@Nullable Pos origin) {
        origin = Objects.requireNonNullElse(origin, Pos.ZERO);
        return coordinateType.convert(vec, origin, relativeX, relativeY, relativeZ);
    }

    /**
     * Gets the location based on the relative fields.
     *
     * @param entity the entity to get the relative position from
     * @return the location
     */
    public @NotNull Vec from(@Nullable Entity entity) {
        if (entity != null) {
            return from(entity.getPosition());
        } else {
            return from(Pos.ZERO);
        }
    }

    /**
     * Shorthand for {@link #from(Pos)}
     * If player uses their position otherwise, {@link Vec#ZERO}
     *
     * @param sender entity
     * @return the position with any relativity
     */
    public @NotNull Vec fromSender(@Nullable CommandSender sender) {
        final var entityPosition = sender instanceof Player ? ((Player) sender).getPosition() : Pos.ZERO;
        return from(entityPosition);
    }

    /**
     * Computes a view {@link Vec} based on the given point's yaw and pitch.
     * If no point is null, a default position {@link Pos#ZERO} is used.
     *
     * @param point The reference position used for computing relative coordinates. If null {@link Pos#ZERO}
     * @return A {@link Vec} with XZ based on the provided position. Y is ignored.
     */
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
     * Shorthand for {@link #fromView(Pos)}
     * @param entity to get the position from, otherwise {@link Pos#ZERO}
     * @return the view.
     */
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

    public enum CoordinateType {
        /**
         * Relative when any XYZ have the relative flag; unless local.
         */
        RELATIVE((relative, origin, relativeX, relativeY, relativeZ) -> {
            final var absolute = Objects.requireNonNullElse(origin, Vec.ZERO);
            final double x = relative.x() + (relativeX ? absolute.x() : 0);
            final double y = relative.y() + (relativeY ? absolute.y() : 0);
            final double z = relative.z() + (relativeZ ? absolute.z() : 0);
            return new Vec(x, y, z);
        }),
        /**
         * Local type used in direction, requires full relatively on XZ/XYZ
         */
        LOCAL((local, origin, relativeX, relativeY, relativeZ) -> {
            final Vec vec1 = new Vec(Math.cos(Math.toRadians(origin.yaw() + 90.0f)), 0, Math.sin(Math.toRadians(origin.yaw() + 90.0f)));
            final Vec a = vec1.mul(Math.cos(Math.toRadians(-origin.pitch()))).withY(Math.sin(Math.toRadians(-origin.pitch())));
            final Vec b = vec1.mul(Math.cos(Math.toRadians(-origin.pitch() + 90.0f))).withY(Math.sin(Math.toRadians(-origin.pitch() + 90.0f)));
            final Vec c = a.cross(b).neg();
            final Vec relativePos = a.mul(local.z()).add(b.mul(local.y())).add(c.mul(local.x()));
            return origin.add(relativePos).asVec();
        }),
        /**
         * Absolute just returns the original vector.
         */
        ABSOLUTE(((vec, origin, relativeX1, relativeY1, relativeZ1) -> vec));

        private final CoordinateConverter converter;

        CoordinateType(CoordinateConverter converter) {
            this.converter = converter;
        }

        private @NotNull Vec convert(Vec vec, Pos origin, boolean relativeX, boolean relativeY, boolean relativeZ) {
            return converter.convert(vec, origin, relativeX, relativeY, relativeZ);
        }
    }

    @FunctionalInterface
    private interface CoordinateConverter {
        @NotNull Vec convert(Vec vec, Pos origin, boolean relativeX, boolean relativeY, boolean relativeZ);
    }
}
