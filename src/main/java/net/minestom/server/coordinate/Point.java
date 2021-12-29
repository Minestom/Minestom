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
 */
public sealed interface Point permits Vec, Pos {

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
     * Gets the floored value of the X component
     *
     * @return the block X
     */
    @Contract(pure = true)
    default int blockX() {
        return (int) Math.floor(x());
    }

    /**
     * Gets the floored value of the X component
     *
     * @return the block X
     */
    @Contract(pure = true)
    default int blockY() {
        return (int) Math.floor(y());
    }

    /**
     * Gets the floored value of the X component
     *
     * @return the block X
     */
    @Contract(pure = true)
    default int blockZ() {
        return (int) Math.floor(z());
    }

    @Contract(pure = true)
    default int chunkX() {
        return ChunkUtils.getChunkCoordinate(x());
    }

    @Contract(pure = true)
    @ApiStatus.Experimental
    default int section() {
        return ChunkUtils.getChunkCoordinate(y());
    }

    @Contract(pure = true)
    default int chunkZ() {
        return ChunkUtils.getChunkCoordinate(z());
    }

    /**
     * Creates a point with a modified X coordinate based on its value.
     *
     * @param operator the operator providing the current X coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    @NotNull Point withX(@NotNull DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified X coordinate.
     *
     * @param x the new X coordinate
     * @return a new point
     */
    @Contract(pure = true)
    @NotNull Point withX(double x);

    /**
     * Creates a point with a modified Y coordinate based on its value.
     *
     * @param operator the operator providing the current Y coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    @NotNull Point withY(@NotNull DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified Y coordinate.
     *
     * @param y the new Y coordinate
     * @return a new point
     */
    @Contract(pure = true)
    @NotNull Point withY(double y);

    /**
     * Creates a point with a modified Z coordinate based on its value.
     *
     * @param operator the operator providing the current Z coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    @NotNull Point withZ(@NotNull DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified Z coordinate.
     *
     * @param z the new Z coordinate
     * @return a new point
     */
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
        return switch (face) {
            case BOTTOM -> sub(0, 1, 0);
            case TOP -> add(0, 1, 0);
            case NORTH -> sub(0, 0, 1);
            case SOUTH -> add(0, 0, 1);
            case WEST -> sub(1, 0, 0);
            case EAST -> add(1, 0, 0);
        };
    }

    @Contract(pure = true)
    default double distanceSquared(double x, double y, double z) {
        return MathUtils.square(x() - x) + MathUtils.square(y() - y) + MathUtils.square(z() - z);
    }

    /**
     * Gets the squared distance between this point and another.
     *
     * @param point the other point
     * @return the squared distance
     */
    @Contract(pure = true)
    default double distanceSquared(@NotNull Point point) {
        return distanceSquared(point.x(), point.y(), point.z());
    }

    @Contract(pure = true)
    default double distance(double x, double y, double z) {
        return Math.sqrt(distanceSquared(x, y, z));
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
        return distance(point.x(), point.y(), point.z());
    }

    default boolean samePoint(double x, double y, double z) {
        return Double.compare(x, x()) == 0 && Double.compare(y, y()) == 0 && Double.compare(z, z()) == 0;
    }

    /**
     * Checks it two points have similar (x/y/z).
     *
     * @param point the point to compare
     * @return true if the two positions are similar
     */
    default boolean samePoint(@NotNull Point point) {
        return samePoint(point.x(), point.y(), point.z());
    }

    /**
     * Gets if the three coordinates {@link #x()}, {@link #y()} and {@link #z()}
     * are equals to {@code 0}.
     *
     * @return true if the three coordinates are zero
     */
    default boolean isZero() {
        return x() == 0 && y() == 0 && z() == 0;
    }

    /**
     * Gets if two points are in the same chunk.
     *
     * @param point the point to compare two
     * @return true if 'this' is in the same chunk as {@code point}
     */
    default boolean sameChunk(@NotNull Point point) {
        return chunkX() == point.chunkX() && chunkZ() == point.chunkZ();
    }

    default boolean sameBlock(int blockX, int blockY, int blockZ) {
        return blockX() == blockX && blockY() == blockY && blockZ() == blockZ;
    }

    /**
     * Gets if two points are in the same chunk.
     *
     * @param point the point to compare two
     * @return true if 'this' is in the same chunk as {@code point}
     */
    default boolean sameBlock(@NotNull Point point) {
        return sameBlock(point.blockX(), point.blockY(), point.blockZ());
    }
}
