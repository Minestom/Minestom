package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a 3D point.
 * <p>
 * Can either be a {@link Pos} or {@link Vec}.
 * Interface will become {@code sealed} in the future.
 */
@ApiStatus.NonExtendable
public interface Point {

    /**
     * Gets the X coordinate.
     *
     * @return the X coordinate
     */
    @Contract(pure = true)
    double x();

    /**
     * Gets the Y coordinate.
     *
     * @return the Y coordinate
     */
    @Contract(pure = true)
    double y();

    /**
     * Gets the Z coordinate.
     *
     * @return the Z coordinate
     */
    @Contract(pure = true)
    double z();

    /**
     * Converts all coordinates to integers.
     *
     * @return a new point representing a block position
     */
    @Contract(pure = true)
    default @NotNull Point asBlockPosition() {
        final int castedY = (int) y();
        return new Vec((int) Math.floor(x()),
                (y() == castedY) ? castedY : castedY + 1,
                (int) Math.floor(z()));
    }
}
