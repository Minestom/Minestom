package net.minestom.server.utils.location;

import net.minestom.server.command.CommandSender;
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
    private final CoordinateType coordinateType;
    private final boolean relativeX, relativeY, relativeZ;

    public RelativeVec(@NotNull Vec vec, @NotNull CoordinateType coordinateType, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this.vec = vec;
        this.coordinateType = coordinateType;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    /**
     * Gets the location based on the relative fields and {@code position}.
     *
     * @param origin the origin position, null if none
     * @return the location
     */
    public @NotNull Vec from(@Nullable Pos origin) {
        origin = Objects.requireNonNullElse(origin, Pos.ZERO);
        return coordinateType.convert(vec, origin, relativeX, relativeY, relativeZ);
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
        if (entity != null) {
            return from(entity.getPosition().add(0, entity.getEyeHeight(), 0));
        } else {
            return from(Pos.ZERO);
        }
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

    public enum CoordinateType {
        RELATIVE((relative, origin, relativeX, relativeY, relativeZ) -> {
            if (!relativeX && !relativeY && !relativeZ) {
                return relative;
            }
            final var absolute = Objects.requireNonNullElse(origin, Vec.ZERO);
            final double x = relative.x() + (relativeX ? absolute.x() : 0);
            final double y = relative.y() + (relativeY ? absolute.y() : 0);
            final double z = relative.z() + (relativeZ ? absolute.z() : 0);
            return new Vec(x, y, z);
        }),
        LOCAL((local, origin, relativeX, relativeY, relativeZ) -> {
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
            return new Vec(double14 + origin.x(),double16 + origin.y(),double18 + origin.z());
        }),
        ABSOLUTE(((vec, origin, relativeX1, relativeY1, relativeZ1) -> vec)),
        UNDEFINED((vec, origin, relativeX, relativeY, relativeZ) -> Vec.ZERO);

        private final CoordinateConverter converter;

        CoordinateType(CoordinateConverter converter) {
            this.converter = converter;
        }

        private @NotNull Vec convert(Vec vec, Pos origin, boolean relativeX, boolean relativeY, boolean relativeZ) {
            return converter.convert(vec, origin, relativeX, relativeY, relativeZ);
        }
    }

    private interface CoordinateConverter {
        @NotNull Vec convert(Vec vec, Pos origin, boolean relativeX, boolean relativeY, boolean relativeZ);
    }
}
