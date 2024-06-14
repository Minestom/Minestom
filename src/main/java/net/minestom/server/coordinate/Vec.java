package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents an immutable 3D vector.
 * <p>
 * To become a value then primitive type.
 */
public record Vec(double x, double y, double z) implements Point {
    public static final Vec ZERO = new Vec(0);
    public static final Vec ONE = new Vec(1);

    public static final double EPSILON = 0.000001;

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
     * Converts a {@link Point} into a {@link Vec}.
     * Will cast if possible, or instantiate a new object.
     *
     * @param point the point to convert
     * @return the converted vector
     */
    public static @NotNull Vec fromPoint(@NotNull Point point) {
        if (point instanceof Vec vec) return vec;
        return new Vec(point.x(), point.y(), point.z());
    }

    /**
     * Creates a new point with coordinated depending on {@code this}.
     *
     * @param operator the operator
     * @return the created point
     */
    @Contract(pure = true)
    public @NotNull Vec apply(@NotNull Operator operator) {
        return operator.apply(x, y, z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withX(@NotNull DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(x), y, z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withX(double x) {
        return new Vec(x, y, z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withY(@NotNull DoubleUnaryOperator operator) {
        return new Vec(x, operator.applyAsDouble(y), z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withY(double y) {
        return new Vec(x, y, z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withZ(@NotNull DoubleUnaryOperator operator) {
        return new Vec(x, y, operator.applyAsDouble(z));
    }

    @Override
    @Contract(pure = true)
    public @NotNull Vec withZ(double z) {
        return new Vec(x, y, z);
    }

    @Override
    public @NotNull Vec add(double x, double y, double z) {
        return new Vec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public @NotNull Vec add(@NotNull Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Vec add(double value) {
        return add(value, value, value);
    }

    @Override
    public @NotNull Vec sub(double x, double y, double z) {
        return new Vec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public @NotNull Vec sub(@NotNull Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Vec sub(double value) {
        return sub(value, value, value);
    }

    @Override
    public @NotNull Vec mul(double x, double y, double z) {
        return new Vec(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public @NotNull Vec mul(@NotNull Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Vec mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public @NotNull Vec div(double x, double y, double z) {
        return new Vec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public @NotNull Vec div(@NotNull Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Vec div(double value) {
        return div(value, value, value);
    }

    @Override
    public @NotNull Vec relative(@NotNull BlockFace face) {
        return (Vec) Point.super.relative(face);
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
     * Returns if a vector is normalized
     *
     * @return whether the vector is normalised
     */
    public boolean isNormalized() {
        return Math.abs(lengthSquared() - 1) < EPSILON;
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
     * Rotates the vector around the x-axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three-dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public @NotNull Vec rotateAroundX(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double newY = angleCos * y - angleSin * z;
        double newZ = angleSin * y + angleCos * z;
        return new Vec(x, newY, newZ);
    }

    /**
     * Rotates the vector around the y-axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three-dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public @NotNull Vec rotateAroundY(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double newX = angleCos * x + angleSin * z;
        double newZ = -angleSin * x + angleCos * z;
        return new Vec(newX, y, newZ);
    }

    /**
     * Rotates the vector around the z axis
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three-dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public @NotNull Vec rotateAroundZ(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double newX = angleCos * x - angleSin * y;
        double newY = angleSin * x + angleCos * y;
        return new Vec(newX, newY, z);
    }

    @Contract(pure = true)
    public @NotNull Vec rotate(double angleX, double angleY, double angleZ) {
        return rotateAroundX(angleX).rotateAroundY(angleY).rotateAroundZ(angleZ);
    }

    @Contract(pure = true)
    public @NotNull Vec rotateFromView(float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(-1 * (yawDegrees + 90));
        double pitch = Math.toRadians(-pitchDegrees);

        double cosYaw = Math.cos(yaw);
        double cosPitch = Math.cos(pitch);
        double sinYaw = Math.sin(yaw);
        double sinPitch = Math.sin(pitch);

        double initialX, initialY, initialZ;
        double x, y, z;

        // Z_Axis rotation (Pitch)
        initialX = x();
        initialY = y();
        x = initialX * cosPitch - initialY * sinPitch;
        y = initialX * sinPitch + initialY * cosPitch;

        // Y_Axis rotation (Yaw)
        initialZ = z();
        initialX = x;
        z = initialZ * cosYaw - initialX * sinYaw;
        x = initialZ * sinYaw + initialX * cosYaw;

        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public @NotNull Vec rotateFromView(@NotNull Pos pos) {
        return rotateFromView(pos.yaw(), pos.pitch());
    }

    /**
     * Rotates the vector around a given arbitrary axis in 3 dimensional space.
     *
     * <p>
     * Rotation will follow the general Right-Hand-Rule, which means rotation
     * will be counterclockwise when the axis is pointing towards the observer.
     * <p>
     * This method will always make sure the provided axis is a unit vector, to
     * not modify the length of the vector when rotating. If you are experienced
     * with the scaling of a non-unit axis vector, you can use
     * {@link Vec#rotateAroundNonUnitAxis(Vec, double)}.
     *
     * @param axis  the axis to rotate the vector around. If the passed vector is
     *              not of length 1, it gets copied and normalized before using it for the
     *              rotation. Please use {@link Vec#normalize()} on the instance before
     *              passing it to this method
     * @param angle the angle to rotate the vector around the axis
     * @return a new vector
     */
    @Contract(pure = true)
    public @NotNull Vec rotateAroundAxis(@NotNull Vec axis, double angle) throws IllegalArgumentException {
        return rotateAroundNonUnitAxis(axis.isNormalized() ? axis : axis.normalize(), angle);
    }

    /**
     * Rotates the vector around a given arbitrary axis in 3 dimensional space.
     *
     * <p>
     * Rotation will follow the general Right-Hand-Rule, which means rotation
     * will be counterclockwise when the axis is pointing towards the observer.
     * <p>
     * Note that the vector length will change accordingly to the axis vector
     * length. If the provided axis is not a unit vector, the rotated vector
     * will not have its previous length. The scaled length of the resulting
     * vector will be related to the axis vector. If you are not perfectly sure
     * about the scaling of the vector, use
     * {@link Vec#rotateAroundAxis(Vec, double)}
     *
     * @param axis  the axis to rotate the vector around.
     * @param angle the angle to rotate the vector around the axis
     * @return a new vector
     */
    @Contract(pure = true)
    public @NotNull Vec rotateAroundNonUnitAxis(@NotNull Vec axis, double angle) throws IllegalArgumentException {
        double x = x(), y = y(), z = z();
        double x2 = axis.x(), y2 = axis.y(), z2 = axis.z();
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double dotProduct = this.dot(axis);

        double newX = x2 * dotProduct * (1d - cosTheta)
                + x * cosTheta
                + (-z2 * y + y2 * z) * sinTheta;
        double newY = y2 * dotProduct * (1d - cosTheta)
                + y * cosTheta
                + (z2 * x - x2 * z) * sinTheta;
        double newZ = z2 * dotProduct * (1d - cosTheta)
                + z * cosTheta
                + (-y2 * x + x2 * y) * sinTheta;

        return new Vec(newX, newY, newZ);
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

    @FunctionalInterface
    public interface Operator {
        /**
         * Checks each axis' value, if it's below {@code Vec#EPSILON} then it gets replaced with {@code 0}
         */
        Operator EPSILON = (x, y, z) -> new Vec(
                Math.abs(x) < Vec.EPSILON ? 0 : x,
                Math.abs(y) < Vec.EPSILON ? 0 : y,
                Math.abs(z) < Vec.EPSILON ? 0 : z
        );

        Operator FLOOR = (x, y, z) -> new Vec(
                Math.floor(x),
                Math.floor(y),
                Math.floor(z)
        );

        @NotNull Vec apply(double x, double y, double z);
    }

    @FunctionalInterface
    public interface Interpolation {
        Interpolation LINEAR = a -> a;
        Interpolation SMOOTH = a -> a * a * (3 - 2 * a);

        double apply(double a);
    }
}
