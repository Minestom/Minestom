package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.coordinate.CoordConversion.globalToBlock;

/**
 * Represents a 3D vector with block-aligned coordinates.
 * <p>
 * Using 12 bytes compared to 24 bytes for {@link Vec}.
 * Ideal for block positions, chunk coordinates, and anything on a grid.
 * <p>
 * Conversion: When constructed from {@code double} values,
 * coordinates are floored to the nearest integer block position.
 * <p>
 * Instances are immutable. All operations return new instances
 * (either {@link BlockVec} for integer results or {@link Vec} where doubles are used).
 *
 * @param blockX the block X coordinate
 * @param blockY the block Y coordinate
 * @param blockZ the block Z coordinate
 */

public record BlockVec(int blockX, int blockY, int blockZ) implements Point {
    public static final BlockVec ZERO = new BlockVec(0);
    public static final BlockVec ONE = new BlockVec(1);
    public static final BlockVec SECTION = new BlockVec(SECTION_SIZE);
    public static final BlockVec REGION = new BlockVec(REGION_SIZE);

    /**
     * Narrows an assumed global coordinate to a block coordinate by flooring the value.
     * <br>
     * Developer Note: Minestom should not call this constructor without explicit warnings.
     *
     * @param x the global x coordinate
     * @param y the global y coordinate
     * @param z the global z coordinate
     */
    public BlockVec(double x, double y, double z) {
        this(globalToBlock(x), globalToBlock(y), globalToBlock(z));
    }

    /**
     * Creates a block vector with the given value for all coordinates.
     * See {@link #BlockVec(double, double, double)} for side effects.
     *
     * @param value the value
     */
    public BlockVec(double value) {
        this(value, value, value);
    }

    /**
     * Creates a block vector from a point.
     *
     * @param point the point
     * @deprecated Use {@link Point#asBlockVec()} instead
     */
    @Deprecated
    public BlockVec(Point point) {
        this(point.blockX(), point.blockY(), point.blockZ());
    }

    /**
     * Creates a block vector with the given value for all coordinates (x/y/z).
     *
     * @param value the value
     */
    public BlockVec(int value) {
        this(value, value, value);
    }

    @Override
    @Contract(pure = true)
    public double x() {
        return blockX;
    }

    @Override
    @Contract(pure = true)
    public double y() {
        return blockY;
    }

    @Override
    @Contract(pure = true)
    public double z() {
        return blockZ;
    }

    /**
     * Applies the given operator to this block vector.
     *
     * @param operator the operator to apply
     * @return the resulting block vector
     */
    public BlockVec apply(Operator operator) {
        return operator.apply(blockX, blockY, blockZ);
    }

    @Override
    @Contract("_ -> new")
    public Vec withX(DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(blockX), blockY, blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withX(double x) {
        return new Vec(x, blockY, blockZ);
    }

    /**
     * Sets the block X coordinate to the given value.
     *
     * @param blockX the block X coordinate
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec withBlockX(int blockX) {
        return new BlockVec(blockX, blockY, blockZ);
    }

    /**
     * Applies the given operator to the block X coordinate.
     *
     * @param operator the operator to apply
     * @return the resulting block vector
     */
    @Contract("_ -> new")
    public BlockVec withBlockX(IntUnaryOperator operator) {
        return new BlockVec(operator.applyAsInt(blockX), blockY, blockZ);
    }

    @Override
    @Contract("_ -> new")
    public Vec withY(DoubleUnaryOperator operator) {
        return new Vec(blockX, operator.applyAsDouble(blockY), blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withY(double y) {
        return new Vec(blockX, y, blockZ);
    }

    /**
     * Sets the block Y coordinate to the given value.
     *
     * @param blockY the block Y coordinate
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec withBlockY(int blockY) {
        return new BlockVec(blockX, blockY, blockZ);
    }

    /**
     * Applies the given operator to the block Y coordinate.
     *
     * @param operator the operator to apply
     * @return the resulting block vector
     */
    @Contract("_ -> new")
    public BlockVec withBlockY(IntUnaryOperator operator) {
        return new BlockVec(blockX, operator.applyAsInt(blockY), blockZ);
    }

    @Override
    @Contract("_ -> new")
    public Vec withZ(DoubleUnaryOperator operator) {
        return new Vec(blockX, blockY, operator.applyAsDouble(blockZ));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withZ(double z) {
        return new Vec(blockX, blockY, z);
    }

    /**
     * Sets the block Z coordinate to the given value.
     *
     * @param blockZ the block Z coordinate
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec withBlockZ(int blockZ) {
        return new BlockVec(blockX, blockY, blockZ);
    }

    /**
     * Applies the given operator to the block Z coordinate.
     *
     * @param operator the operator to apply
     * @return the resulting block vector
     */
    @Contract("_ -> new")
    public BlockVec withBlockZ(IntUnaryOperator operator) {
        return new BlockVec(blockX, blockY, operator.applyAsInt(blockZ));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec add(double x, double y, double z) {
        return new Vec(blockX + x, blockY + y, blockZ + z);
    }

    /**
     * Adds the given block XYZ to this block vector.
     *
     * @param blockX the block X to add
     * @param blockY the block Y to add
     * @param blockZ the block Z to add
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec add(int blockX, int blockY, int blockZ) {
        return new BlockVec(this.blockX + blockX, this.blockY + blockY, this.blockZ + blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec add(Point point) {
        return new Vec(blockX + point.x(), blockY + point.y(), blockZ + point.z());
    }

    /**
     * Adds the given block vector to this block vector.
     *
     * @param blockVec the block vector to add
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec add(BlockVec blockVec) {
        return new BlockVec(blockX + blockVec.blockX, blockY + blockVec.blockY, blockZ + blockVec.blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec add(double value) {
        return add(value, value, value);
    }

    /**
     * Adds the given integer value to all coordinates of this block vector.
     *
     * @param value the value to add
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec add(int value) {
        return new BlockVec(blockX + value, blockY + value, blockZ + value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec sub(double x, double y, double z) {
        return new Vec(blockX - x, blockY - y, blockZ - z);
    }

    /**
     * Subtracts the given block XYZ from this block vector.
     *
     * @param blockX the block X to subtract
     * @param blockY the block Y to subtract
     * @param blockZ the block Z to subtract
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec sub(int blockX, int blockY, int blockZ) {
        return new BlockVec(this.blockX - blockX, this.blockY - blockY, this.blockZ - blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec sub(Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    /**
     * Subtracts the given block vector from this block vector.
     *
     * @param blockVec the block vector to subtract
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec sub(BlockVec blockVec) {
        return new BlockVec(blockX - blockVec.blockX, blockY - blockVec.blockY, blockZ - blockVec.blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec sub(double value) {
        return sub(value, value, value);
    }

    /**
     * Subtracts the given integer value from all coordinates of this block vector.
     *
     * @param value the value to subtract
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec sub(int value) {
        return new BlockVec(blockX - value, blockY - value, blockZ - value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec mul(double x, double y, double z) {
        return new Vec(blockX * x, blockY * y, blockZ * z);
    }

    /**
     * Multiplies this block vector by the given integer values.
     *
     * @param blockX the block x to multiply by
     * @param blockY the block y to multiply by
     * @param blockZ the block z to multiply by
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec mul(int blockX, int blockY, int blockZ) {
        return new BlockVec(this.blockX * blockX, this.blockY * blockY, this.blockZ * blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec mul(Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    /**
     * Multiplies this block vector by another block vector.
     *
     * @param blockVec the block vector to multiply by
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec mul(BlockVec blockVec) {
        return new BlockVec(blockX * blockVec.blockX, blockY * blockVec.blockY, blockZ * blockVec.blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec mul(double value) {
        return mul(value, value, value);
    }

    /**
     * Multiplies this block vector by the given integer value.
     *
     * @param value the value to multiply by
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec mul(int value) {
        return mul(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec div(double x, double y, double z) {
        return new Vec(blockX / x, blockY / y, blockZ / z);
    }

    /**
     * Divides this block vector by the given integer values.
     *
     * @param blockX the x divisor
     * @param blockY the y divisor
     * @param blockZ the z divisor
     * @return the resulting block vector
     * @throws ArithmeticException if any of the divisors is zero
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec div(int blockX, int blockY, int blockZ) {
        return new BlockVec(this.blockX / blockX, this.blockY / blockY, this.blockZ / blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    /**
     * Divides this block vector by another block vector.
     *
     * @param blockVec the block vector divisor
     * @return the resulting block vector
     * @throws ArithmeticException if any component of the divisor is zero
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec div(BlockVec blockVec) {
        return new BlockVec(blockX / blockVec.blockX, blockY / blockVec.blockY, blockZ / blockVec.blockZ);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec div(double value) {
        return div(value, value, value);
    }

    /**
     * Divides this block vector by the given integer value.
     *
     * @param value the divisor
     * @return the resulting block vector
     * @throws ArithmeticException if the divisor is zero
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec div(int value) {
        return div(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public BlockVec relative(BlockFace face) {
        // Cant use super because of return type of #add(double, double, double), use #add(int, int, int) instead
        final Direction direction = face.toDirection();
        return add(direction.normalX(), direction.normalY(), direction.normalZ());
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public BlockVec neg() {
        return new BlockVec(-blockX, -blockY, -blockZ);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public BlockVec abs() {
        return new BlockVec(Math.abs(blockX), Math.abs(blockY), Math.abs(blockZ));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec min(Point point) {
        return new Vec(Math.min(blockX, point.x()), Math.min(blockY, point.y()), Math.min(blockZ, point.z()));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec min(double x, double y, double z) {
        return new Vec(Math.min(blockX, x), Math.min(blockY, y), Math.min(blockZ, z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec min(double value) {
        return new Vec(Math.min(blockX, value), Math.min(blockY, value), Math.min(blockZ, value));
    }

    /**
     * Calculates the minimum between this block vector and another block vector.
     *
     * @param point the other block vector
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec min(BlockVec point) {
        return new BlockVec(Math.min(blockX, point.blockX()), Math.min(blockY, point.blockY()), Math.min(blockZ, point.blockZ()));
    }

    /**
     * Calculates the minimum between this block vector and the given block coordinates.
     *
     * @param blockX the blockX
     * @param blockY the blockY
     * @param blockZ the blockZ
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec min(int blockX, int blockY, int blockZ) {
        return new BlockVec(Math.min(this.blockX, blockX), Math.min(this.blockY, blockY), Math.min(this.blockZ, blockZ));
    }

    /**
     * Calculates the minimum between this block vector and the given integer value.
     *
     * @param value the value
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec min(int value) {
        return new BlockVec(Math.min(blockX, value), Math.min(blockY, value), Math.min(blockZ, value));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec max(Point point) {
        return new Vec(Math.max(blockX, point.x()), Math.max(blockY, point.y()), Math.max(blockZ, point.z()));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec max(double x, double y, double z) {
        return new Vec(Math.max(blockX, x), Math.max(blockY, y), Math.max(blockZ, z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec max(double value) {
        return new Vec(Math.max(blockX, value), Math.max(blockY, value), Math.max(blockZ, value));
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Vec normalize() {
        final double length = length();
        return new Vec(blockX / length, blockY / length, blockZ / length);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec cross(Point point) {
        return new Vec(blockY * point.z() - blockZ * point.y(),
                blockZ * point.x() - blockX * point.z(),
                blockX * point.y() - blockY * point.x());
    }

    /**
     * Calculates the cross product of this point with another. The cross
     * product is defined as:
     * <ul>
     * <li>x = y1 * z2 - y2 * z1
     * <li>y = z1 * x2 - z2 * x1
     * <li>z = x1 * y2 - x2 * y1
     * </ul>
     *
     * @param point the other point
     * @return the cross product point
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec cross(BlockVec point) {
        return new BlockVec(blockY * point.blockZ - blockZ * point.blockY,
                blockZ * point.blockX - blockX * point.blockZ,
                blockX * point.blockY - blockY * point.blockX);
    }

    @Override
    @Contract(pure = true, value = "_, _ -> new")
    public Vec lerp(Point point, double alpha) {
        return new Vec(blockX + (alpha * (point.x() - blockX)),
                blockY + (alpha * (point.y() - blockY)),
                blockZ + (alpha * (point.z() - blockZ)));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec lerp(Point point, double alpha, Easing easing) {
        return (Vec) Point.super.lerp(point, alpha, easing);
    }

    /**
     * Calculates the maximum between this block vector and another block vector.
     *
     * @param point the other block vector
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec max(BlockVec point) {
        return new BlockVec(Math.max(blockX, point.blockX()), Math.max(blockY, point.blockY()), Math.max(blockZ, point.blockZ()));
    }

    /**
     * Calculates the maximum between this block vector and the given block coordinates (x/y/z).
     *
     * @param blockX the block X
     * @param blockY the block Y
     * @param blockZ the block Z
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_, _, _ -> new")
    public BlockVec max(int blockX, int blockY, int blockZ) {
        return new BlockVec(Math.max(this.blockX, blockX), Math.max(this.blockY, blockY), Math.max(this.blockZ, blockZ));
    }

    /**
     * Calculates the maximum between this block vector and the given integer value.
     *
     * @param value the value
     * @return the resulting block vector
     */
    @Contract(pure = true, value = "_ -> new")
    public BlockVec max(int value) {
        return new BlockVec(Math.max(blockX, value), Math.max(blockY, value), Math.max(blockZ, value));
    }

    /**
     * Checks if two block positions have the same coordinates (x/y/z).
     *
     * @param blockX the block X coordinate
     * @param blockY the block Y coordinate
     * @param blockZ the block Z coordinate
     * @return true if the coordinates are the same
     */
    @Contract(pure = true)
    public boolean samePoint(int blockX, int blockY, int blockZ) {
        return this.blockX == blockX && this.blockY == blockY && this.blockZ == blockZ;
    }

    /**
     * Checks if two block vectors have the same coordinates (x/y/z).
     *
     * @param blockVec the other block vector
     * @return true if the coordinates are the same
     */
    @Contract(pure = true)
    public boolean samePoint(BlockVec blockVec) {
        return blockX == blockVec.blockX && blockY == blockVec.blockY && blockZ == blockVec.blockZ;
    }

    /**
     * Does nothing as this is already a {@link BlockVec}.
     * <p>
     * Marked as deprecated to warn against redundant usage.
     *
     * @return this block vector
     */
    @Deprecated
    @Override
    @Contract(pure = true, value = "-> this")
    public BlockVec asBlockVec() {
        return this;
    }

    /**
     * A functional interface representing an operation on the components of a {@link BlockVec}.
     */
    @FunctionalInterface
    public interface Operator {
        /**
         * Creates an operator from the given {@link IntUnaryOperator}.
         *
         * @param operator the operator to convert
         * @return the resulting operator
         */
        static Operator operator(IntUnaryOperator operator) {
            return (blockX, blockY, blockZ) -> new BlockVec(
                    operator.applyAsInt(blockX),
                    operator.applyAsInt(blockY),
                    operator.applyAsInt(blockZ));
        }

        /**
         * Applies this operator to the given block coordinates.
         *
         * @param blockX the blockX component
         * @param blockY the blockY component
         * @param blockZ the blockZ component
         * @return the resulting block vector
         */
        BlockVec apply(int blockX, int blockY, int blockZ);
    }
}
