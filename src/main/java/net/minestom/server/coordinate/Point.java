package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;

import static net.minestom.server.coordinate.CoordConversion.*;

/**
 * Represents a 3D point.
 */
public sealed interface Point permits Vec, Pos, BlockVec {

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
        return globalToBlock(x());
    }

    /**
     * Gets the floored value of the Y component
     *
     * @return the block Y
     */
    @Contract(pure = true)
    default int blockY() {
        return globalToBlock(y());
    }

    /**
     * Gets the floored value of the Z component
     *
     * @return the block Z
     */
    @Contract(pure = true)
    default int blockZ() {
        return globalToBlock(z());
    }

    @Contract(pure = true)
    default int sectionX() {
        return globalToSection(blockX());
    }

    @Contract(pure = true)
    default int sectionY() {
        return globalToSection(blockY());
    }

    @Contract(pure = true)
    default int sectionZ() {
        return globalToSection(blockZ());
    }

    @Contract(pure = true)
    default int chunkX() {
        return sectionX();
    }

    @Contract(pure = true)
    default int chunkZ() {
        return sectionZ();
    }

    @Contract(pure = true)
    default int regionX() {
        return globalToRegion(blockX());
    }

    @Contract(pure = true)
    default int regionZ() {
        return globalToRegion(blockZ());
    }

    /**
     * @deprecated use {@link #sectionY()} instead.
     */
    @Deprecated
    @Contract(pure = true)
    default int section() {
        return sectionY();
    }

    /**
     * Creates a point with a modified X coordinate based on its value.
     *
     * @param operator the operator providing the current X coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    Point withX(DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified X coordinate.
     *
     * @param x the new X coordinate
     * @return a new point
     */
    @Contract(pure = true)
    Point withX(double x);

    /**
     * Creates a point with a modified Y coordinate based on its value.
     *
     * @param operator the operator providing the current Y coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    Point withY(DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified Y coordinate.
     *
     * @param y the new Y coordinate
     * @return a new point
     */
    @Contract(pure = true)
    Point withY(double y);

    /**
     * Creates a point with a modified Z coordinate based on its value.
     *
     * @param operator the operator providing the current Z coordinate and returning the new
     * @return a new point
     */
    @Contract(pure = true)
    Point withZ(DoubleUnaryOperator operator);

    /**
     * Creates a point with the specified Z coordinate.
     *
     * @param z the new Z coordinate
     * @return a new point
     */
    @Contract(pure = true)
    Point withZ(double z);

    @Contract(pure = true)
    Point add(double x, double y, double z);

    @Contract(pure = true)
    Point add(Point point);

    @Contract(pure = true)
    Point add(double value);

    @Contract(pure = true)
    Point sub(double x, double y, double z);

    @Contract(pure = true)
    Point sub(Point point);

    @Contract(pure = true)
    Point sub(double value);

    @Contract(pure = true)
    Point mul(double x, double y, double z);

    @Contract(pure = true)
    Point mul(Point point);

    @Contract(pure = true)
    Point mul(double value);

    @Contract(pure = true)
    Point div(double x, double y, double z);

    @Contract(pure = true)
    Point div(Point point);

    @Contract(pure = true)
    Point div(double value);

    @Contract(pure = true)
    default Point relative(BlockFace face) {
        final Direction direction = face.toDirection();
        return add(direction.normalX(), direction.normalY(), direction.normalZ());
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
    default double distanceSquared(Point point) {
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
    default double distance(Point point) {
        return distance(point.x(), point.y(), point.z());
    }

    default boolean samePoint(double x, double y, double z) {
        return x == x() && y == y() && z == z();
    }

    /**
     * Checks it two points have similar (x/y/z).
     *
     * @param point the point to compare
     * @return true if the two positions are similar
     */
    default boolean samePoint(Point point) {
        return samePoint(point.x(), point.y(), point.z());
    }

    /**
     * Checks it two points have similar (x/y/z) coordinates within a given epsilon.
     *
     * @param x       the x coordinate to compare
     * @param y       the y coordinate to compare
     * @param z       the z coordinate to compare
     * @param epsilon the maximum difference allowed between the two points (exclusive)
     * @return true if the two positions are similar within the epsilon
     * @throws IllegalArgumentException if epsilon is less than or equal to 0
     */
    default boolean samePoint(double x, double y, double z, double epsilon) {
        Check.argCondition(epsilon <= 0, "Epsilon must be greater than 0 but found {0}", epsilon);
        return Math.abs(x - x()) < epsilon && Math.abs(y - y()) < epsilon && Math.abs(z - z()) < epsilon;
    }

    /**
     * Checks it two points have similar (x/y/z) coordinates within a given epsilon.
     *
     * @param point   the point to compare
     * @param epsilon the maximum difference allowed between the two points (exclusive)
     * @return true if the two positions are similar within the epsilon
     * @throws IllegalArgumentException if epsilon is less than or equal to 0
     */
    default boolean samePoint(Point point, double epsilon) {
        return samePoint(point.x(), point.y(), point.z(), epsilon);
    }

    /**
     * Checks if the three coordinates {@link #x()}, {@link #y()} and {@link #z()}
     * are equal to {@code 0}.
     *
     * @return true if the three coordinates are zero
     */
    default boolean isZero() {
        return x() == 0 && y() == 0 && z() == 0;
    }

    /**
     * Checks if two points are in the same chunk.
     *
     * @param point the point to compare to
     * @return true if 'this' is in the same chunk as {@code point}
     */
    default boolean sameChunk(Point point) {
        return chunkX() == point.chunkX() && chunkZ() == point.chunkZ();
    }

    default boolean sameBlock(int blockX, int blockY, int blockZ) {
        return blockX() == blockX && blockY() == blockY && blockZ() == blockZ;
    }

    /**
     * Checks if two points are in the same block.
     *
     * @param point the point to compare to
     * @return true if 'this' is in the same block as {@code point}
     */
    default boolean sameBlock(Point point) {
        return sameBlock(point.blockX(), point.blockY(), point.blockZ());
    }

    @Contract(pure = true)
    default Pos asPos() {
        return switch (this) {
            case Pos pos -> pos;
            case Vec vec -> new Pos(vec.x(), vec.y(), vec.z());
            case BlockVec blockVec -> new Pos(blockVec.blockX(), blockVec.blockY(), blockVec.blockZ());
        };
    }

    @Contract(pure = true)
    default Vec asVec() {
        return switch (this) {
            case Vec vec -> vec;
            case Pos pos -> new Vec(pos.x(), pos.y(), pos.z());
            case BlockVec blockVec -> new Vec(blockVec.blockX(), blockVec.blockY(), blockVec.blockZ());
        };
    }

    @Contract(pure = true)
    default BlockVec asBlockVec() {
        return switch (this) {
            case BlockVec blockVec -> blockVec;
            case Pos pos -> new BlockVec(pos.blockX(), pos.blockY(), pos.blockZ());
            case Vec vec -> new BlockVec(vec.blockX(), vec.blockY(), vec.blockZ());
        };
    }
}
