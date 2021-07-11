package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

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

    @Contract(pure = true)
    default int blockX() {
        return MathUtils.floor(x());
    }

    @Contract(pure = true)
    default int blockY() {
        return MathUtils.floor(y());
    }

    @Contract(pure = true)
    default int blockZ() {
        return MathUtils.floor(z());
    }

    @Contract(pure = true)
    @NotNull Point withX(@NotNull DoubleUnaryOperator operator);

    @Contract(pure = true)
    @NotNull Point withX(double x);

    @Contract(pure = true)
    @NotNull Point withY(@NotNull DoubleUnaryOperator operator);

    @Contract(pure = true)
    @NotNull Point withY(double y);

    @Contract(pure = true)
    @NotNull Point withZ(@NotNull DoubleUnaryOperator operator);

    @Contract(pure = true)
    @NotNull Point withZ(double z);

    @Contract(pure = true)
    @NotNull Point add(double x, double y, double z);

    @Contract(pure = true)
    @NotNull Point add(@NotNull Point point);

    @Contract(pure = true)
    @NotNull Point add(double value);

    @Contract(pure = true)
    @NotNull Point sub(double x, double y, double z);

    @Contract(pure = true)
    @NotNull Point sub(@NotNull Point point);

    @Contract(pure = true)
    @NotNull Point sub(double value);

    @Contract(pure = true)
    @NotNull Point mul(double x, double y, double z);

    @Contract(pure = true)
    @NotNull Point mul(@NotNull Point point);

    @Contract(pure = true)
    @NotNull Point mul(double value);

    @Contract(pure = true)
    @NotNull Point div(double x, double y, double z);

    @Contract(pure = true)
    @NotNull Point div(@NotNull Point point);

    @Contract(pure = true)
    @NotNull Point div(double value);

    @Contract(pure = true)
    default @NotNull Point relative(@NotNull BlockFace face) {
        switch (face) {
            case BOTTOM:
                return sub(0, 1, 0);
            case TOP:
                return add(0, 1, 0);
            case NORTH:
                return sub(0, 0, 1);
            case SOUTH:
                return add(0, 0, 1);
            case WEST:
                return sub(1, 0, 0);
            case EAST:
                return add(1, 0, 0);
        }
        return this; // should never be called
    }

    /**
     * Gets the distance between this point and another. The value of this
     * method is not cached and uses a costly square-root function, so do not
     * repeatedly call this method to get the vector's magnitude. NaN will be
     * returned if the inner result of the sqrt() function overflows, which
     * will be caused if the distance is too long.
     *
     * @param point the other point
     * @return the distance
     */
    @Contract(pure = true)
    default double distance(@NotNull Point point) {
        return Math.sqrt(MathUtils.square(x() - point.x()) +
                MathUtils.square(y() - point.y()) +
                MathUtils.square(z() - point.z()));
    }

    /**
     * Gets the squared distance between this point and another.
     *
     * @param point the other point
     * @return the squared distance
     */
    @Contract(pure = true)
    default double distanceSquared(@NotNull Point point) {
        return MathUtils.square(x() - point.x()) +
                MathUtils.square(y() - point.y()) +
                MathUtils.square(z() - point.z());
    }

    /**
     * Checks it two points have similar (x/y/z).
     *
     * @param point the point to compare
     * @return true if the two positions are similar
     */
    default boolean samePoint(@NotNull Point point) {
        return Double.compare(point.x(), x()) == 0 &&
                Double.compare(point.y(), y()) == 0 &&
                Double.compare(point.z(), z()) == 0;
    }

    default boolean isZero() {
        return x() == 0 && y() == 0 && z() == 0;
    }

    /**
     * Gets if two points are in the same chunk.
     *
     * @param point the point to compare two
     * @return true if 'this' is in the same chunk as {@code position}
     */
    default boolean inSameChunk(@NotNull Point point) {
        return ChunkUtils.getChunkCoordinate(x()) == ChunkUtils.getChunkCoordinate(point.x()) &&
                ChunkUtils.getChunkCoordinate(z()) == ChunkUtils.getChunkCoordinate(point.z());
    }
}
