package net.minestom.server.utils.location;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minestom.server.command.builder.arguments.relative.ArgumentRelative.*;

/**
 * Represents a location which can have fields relative to an {@link Entity} position.
 */
public final class RelativeVec {

    private final Vec vec;
    private final boolean relativeX, relativeY, relativeZ;

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

    public static RelativeVec parse(String[] input) throws ArgumentSyntaxException {
        // Check if the value has enough element to be correct
        if (input.length != 3 && input.length != 2) {
            throw new ArgumentSyntaxException("Invalid number of values", String.join(StringUtils.SPACE, input), INVALID_NUMBER_COUNT_ERROR);
        }

        double[] coordinates = new double[input.length];
        boolean[] isRelative = new boolean[input.length];
        for (int i = 0; i < input.length; i++) {
            final String element = input[i];
            try {
                if (element.startsWith(RELATIVE_CHAR)) {
                    isRelative[i] = true;

                    if (element.length() != RELATIVE_CHAR.length()) {
                        final String potentialNumber = element.substring(1);
                        coordinates[i] = Float.parseFloat(potentialNumber);
                    }
                } else {
                    coordinates[i] = Float.parseFloat(element);
                }
            } catch (NumberFormatException e) {
                throw new ArgumentSyntaxException("Invalid number", String.join(StringUtils.SPACE, input), INVALID_NUMBER_ERROR);
            }
        }

        return new RelativeVec(input.length == 3 ? new Vec(coordinates[0], coordinates[1], coordinates[2]) : new Vec(coordinates[0], coordinates[1]),
                isRelative[0], input.length == 3 && isRelative[1], isRelative[input.length == 3 ? 2 : 1]);
    }
}
