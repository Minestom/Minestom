package net.minestom.server.utils.incubator;

import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable 3D vector.
 * <p>
 * To become record and primitive.
 */
public final class Vec implements Point {
    public static final Vec ZERO = new Vec(0);
    public static final Vec ONE = new Vec(1);

    private final double x, y, z;

    /**
     * Creates a new vec with the 3 coordinates set.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Vec(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new vec with the [x;z] coordinates set. Y is set to 0.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vec(double x, double z) {
        this(x, 0, z);
    }

    /**
     * Creates a vec with all 3 coordinates sharing the same value.
     *
     * @param value the coordinates
     */
    public Vec(double value) {
        this(value, value, value);
    }

    /**
     * Creates a new vec with coordinated depending on {@code this}.
     *
     * @param operator the operator
     * @return the created vec
     */
    @Contract(pure = true)
    public @NotNull Vec with(@NotNull Operator operator) {
        return operator.apply(x, y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec withX(@NotNull DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(x), y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec withX(double x) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec withY(@NotNull DoubleUnaryOperator operator) {
        return new Vec(x, operator.applyAsDouble(y), z);
    }

    @Contract(pure = true)
    public @NotNull Vec withY(double y) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec withZ(@NotNull DoubleUnaryOperator operator) {
        return new Vec(x, y, operator.applyAsDouble(z));
    }

    @Contract(pure = true)
    public @NotNull Vec withZ(double z) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec add(@NotNull Point point) {
        return new Vec(x + point.x(), y + point.y(), z + point.z());
    }

    @Contract(pure = true)
    public @NotNull Vec add(double value) {
        return new Vec(x + value, y + value, z + value);
    }

    @Contract(pure = true)
    public @NotNull Vec sub(@NotNull Point point) {
        return new Vec(x - point.x(), y - point.y(), z - point.z());
    }

    @Contract(pure = true)
    public @NotNull Vec sub(double value) {
        return new Vec(x - value, y - value, z - value);
    }

    @Contract(pure = true)
    public @NotNull Vec mul(@NotNull Point point) {
        return new Vec(x * point.x(), y * point.y(), z * point.z());
    }

    @Contract(pure = true)
    public @NotNull Vec mul(double value) {
        return new Vec(x * value, y * value, z * value);
    }

    @Contract(pure = true)
    public @NotNull Vec div(@NotNull Point point) {
        return new Vec(x / point.x(), y / point.y(), z / point.z());
    }

    @Contract(pure = true)
    public @NotNull Vec div(double value) {
        return new Vec(x / value, y / value, z / value);
    }

    @Contract(pure = true)
    public @NotNull Vec neg() {
        return new Vec(-x, -y, -z);
    }

    @Contract(pure = true)
    public @NotNull Vec abs() {
        return new Vec(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Contract(pure = true)
    public @NotNull Vec min(@NotNull Point point) {
        return new Vec(Math.min(x, point.x()), Math.min(y, point.y()), Math.min(z, point.z()));
    }

    @Contract(pure = true)
    public @NotNull Vec min(double value) {
        return new Vec(Math.min(x, value), Math.min(y, value), Math.min(z, value));
    }

    @Contract(pure = true)
    public @NotNull Vec max(@NotNull Point point) {
        return new Vec(Math.max(x, point.x()), Math.max(y, point.y()), Math.max(z, point.z()));
    }

    @Contract(pure = true)
    public @NotNull Vec max(double value) {
        return new Vec(Math.max(x, value), Math.max(y, value), Math.max(z, value));
    }

    @Contract(pure = true)
    public Vec apply(@NotNull UnaryOperator<@NotNull Vec> operator) {
        return operator.apply(this);
    }

    @Contract(pure = true)
    public @NotNull Pos asPosition() {
        return new Pos(x, y, z);
    }

    /**
     * Gets the magnitude of the vector squared.
     *
     * @return the magnitude
     */
    @Contract(pure = true)
    public double lengthSquared() {
        return MathUtils.square(x) + MathUtils.square(y) + MathUtils.square(z);
    }

    /**
     * Gets the magnitude of the vector, defined as sqrt(x^2+y^2+z^2). The
     * value of this method is not cached and uses a costly square-root
     * function, so do not repeatedly call this method to get the vector's
     * magnitude. NaN will be returned if the inner result of the sqrt()
     * function overflows, which will be caused if the length is too long.
     *
     * @return the magnitude
     */
    @Contract(pure = true)
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Converts this vector to a unit vector (a vector with length of 1).
     *
     * @return the same vector
     */
    @Contract(pure = true)
    public @NotNull Vec normalize() {
        final double length = length();
        return new Vec(x / length, y / length, z / length);
    }

    /**
     * Gets the distance between this vector and another. The value of this
     * method is not cached and uses a costly square-root function, so do not
     * repeatedly call this method to get the vector's magnitude. NaN will be
     * returned if the inner result of the sqrt() function overflows, which
     * will be caused if the distance is too long.
     *
     * @param vec the other vector
     * @return the distance
     */
    @Contract(pure = true)
    public double distance(@NotNull Vec vec) {
        return Math.sqrt(MathUtils.square(x - vec.x) +
                MathUtils.square(y - vec.y) +
                MathUtils.square(z - vec.z));
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param vec the other vector
     * @return the squared distance
     */
    @Contract(pure = true)
    public double distanceSquared(@NotNull Vec vec) {
        return MathUtils.square(x - vec.x) +
                MathUtils.square(y - vec.y) +
                MathUtils.square(z - vec.z);
    }

    /**
     * Gets the angle between this vector and another in radians.
     *
     * @param vec the other vector
     * @return angle in radians
     */
    @Contract(pure = true)
    public double angle(@NotNull Vec vec) {
        final double dot = MathUtils.clamp(dot(vec) / (length() * vec.length()), -1.0, 1.0);
        return Math.acos(dot);
    }

    /**
     * Calculates the dot product of this vector with another. The dot product
     * is defined as x1*x2+y1*y2+z1*z2. The returned value is a scalar.
     *
     * @param vec the other vector
     * @return dot product
     */
    @Contract(pure = true)
    public double dot(@NotNull Vec vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    /**
     * Calculates the cross product of this vector with another. The cross
     * product is defined as:
     * <ul>
     * <li>x = y1 * z2 - y2 * z1
     * <li>y = z1 * x2 - z2 * x1
     * <li>z = x1 * y2 - x2 * y1
     * </ul>
     *
     * @param o the other vector
     * @return the same vector
     */
    @Contract(pure = true)
    public @NotNull Vec cross(@NotNull Vec o) {
        return new Vec(y * o.z - o.y * z,
                z * o.x - o.z * x,
                x * o.y - o.x * y);
    }
    
    /**
     * Rotates the vector around the x axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @NotNull
    public Vec rotateAroundX(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        
        double newY = angleCos * y - angleSin * z;
        double newZ = angleSin * y + angleCos * z;
        return new Vec(x, newY, newZ);
    }

    /**
     * Rotates the vector around the y axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @NotNull
    public Vec rotateAroundY(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        
        double newX =  angleCos * x + angleSin * z;
        double newZ = -angleSin * x + angleCos * z;
        return new Vec(newX, y, newZ);
    }

    /**
     * Rotates the vector around the z axis
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @NotNull
    public Vec rotateAroundZ(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        
        double newX = angleCos * x - angleSin * y;
        double newY = angleSin * x + angleCos * y;
        return new Vec(newX, newY, z);
    }

    /**
     * Calculates a linear interpolation between this vector with another
     * vector.
     *
     * @param vec   the other vector
     * @param alpha The alpha value, must be between 0.0 and 1.0
     * @return Linear interpolated vector
     */
    @Contract(pure = true)
    public @NotNull Vec lerp(@NotNull Vec vec, double alpha) {
        return new Vec(x + (alpha * (vec.x - x)),
                y + (alpha * (vec.y - y)),
                z + (alpha * (vec.z - z)));
    }

    @Contract(pure = true)
    public @NotNull Vec interpolate(@NotNull Vec target, double alpha, @NotNull Interpolation interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec vec = (Vec) o;
        return Double.compare(vec.x, x) == 0 && Double.compare(vec.y, y) == 0 && Double.compare(vec.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @FunctionalInterface
    public interface Operator {
        @NotNull Vec apply(double x, double y, double z);
    }

    @FunctionalInterface
    public interface Interpolation {
        Interpolation LINEAR = a -> a;
        Interpolation SMOOTH = a -> a * a * (3 - 2 * a);

        double apply(double a);
    }
}
