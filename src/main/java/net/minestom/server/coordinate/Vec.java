package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents a 3D vector with double-precision coordinates.
 * <p>
 * This is the fundamental coordinate type for precise spatial calculations.
 * Supports standard vector operations including dot product, cross product,
 * normalization, and rotation.
 *
 * @param x the X coordinate
 * @param y the Y coordinate
 * @param z the Z coordinate
 */
public record Vec(double x, double y, double z) implements Point {
    public static final Vec ZERO = new Vec(0);
    public static final Vec ONE = new Vec(1);
    public static final Vec SECTION = new Vec(SECTION_SIZE);
    public static final Vec CHUNK = new Vec(SECTION_SIZE, SECTION_SIZE);
    public static final Vec REGION = new Vec(REGION_SIZE, REGION_SIZE);

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
     * @deprecated use {@link Point#asVec()} instead
     */
    @Deprecated
    public static Vec fromPoint(Point point) {
        if (point instanceof Vec vec) return vec;
        return new Vec(point.x(), point.y(), point.z());
    }

    /**
     * Applies the given operator to this vector's coordinates (x/y/z).
     *
     * @param operator the operator to apply
     * @return the resulting vector
     */
    public Vec apply(Operator operator) {
        return operator.apply(x, y, z);
    }

    @Override
    @Contract("_ -> new")
    public Vec withX(DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(x), y, z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withX(double x) {
        return new Vec(x, y, z);
    }

    @Override
    @Contract("_ -> new")
    public Vec withY(DoubleUnaryOperator operator) {
        return new Vec(x, operator.applyAsDouble(y), z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withY(double y) {
        return new Vec(x, y, z);
    }

    @Override
    @Contract("_ -> new")
    public Vec withZ(DoubleUnaryOperator operator) {
        return new Vec(x, y, operator.applyAsDouble(z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec withZ(double z) {
        return new Vec(x, y, z);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec add(double x, double y, double z) {
        return new Vec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec add(Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec add(double value) {
        return add(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec sub(double x, double y, double z) {
        return new Vec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec sub(Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec sub(double value) {
        return sub(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec mul(double x, double y, double z) {
        return new Vec(this.x * x, this.y * y, this.z * z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec mul(Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec mul(double value) {
        return mul(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec div(double x, double y, double z) {
        return new Vec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec div(double value) {
        return div(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec relative(BlockFace face) {
        return (Vec) Point.super.relative(face);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Vec neg() {
        return new Vec(-x, -y, -z);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Vec abs() {
        return new Vec(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec min(Point point) {
        return new Vec(Math.min(x, point.x()), Math.min(y, point.y()), Math.min(z, point.z()));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec min(double x, double y, double z) {
        return new Vec(Math.min(this.x, x), Math.min(this.y, y), Math.min(this.z, z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec min(double value) {
        return new Vec(Math.min(x, value), Math.min(y, value), Math.min(z, value));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec max(Point point) {
        return new Vec(Math.max(x, point.x()), Math.max(y, point.y()), Math.max(z, point.z()));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec max(double x, double y, double z) {
        return new Vec(Math.max(this.x, x), Math.max(this.y, y), Math.max(this.z, z));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec max(double value) {
        return new Vec(Math.max(x, value), Math.max(y, value), Math.max(z, value));
    }

    /**
     * @deprecated use {@link Point#asPos()} instead.
     */
    @Deprecated
    @Contract(pure = true)
    public Pos asPosition() {
        return new Pos(x, y, z);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Vec normalize() {
        final double length = length();
        return new Vec(x / length, y / length, z / length);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Vec cross(Point point) {
        return new Vec(y * point.z() - point.y() * z,
                z * point.x() - point.z() * x,
                x * point.y() - point.x() * y);
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
    public Vec rotateAroundX(double angle) {
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
    public Vec rotateAroundY(double angle) {
        final double angleCos = Math.cos(angle);
        final double angleSin = Math.sin(angle);

        final double newX = angleCos * x + angleSin * z;
        final double newZ = -angleSin * x + angleCos * z;
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
    public Vec rotateAroundZ(double angle) {
        final double angleCos = Math.cos(angle);
        final double angleSin = Math.sin(angle);

        final double newX = angleCos * x - angleSin * y;
        final double newY = angleSin * x + angleCos * y;
        return new Vec(newX, newY, z);
    }

    /**
     * Rotates the vector around the x, y, and z axes.
     *
     * @param angleX the angle to rotate around the x-axis in radians
     * @param angleY the angle to rotate around the y-axis in radians
     * @param angleZ the angle to rotate around the z-axis in radians
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public Vec rotate(double angleX, double angleY, double angleZ) {
        return rotateAroundX(angleX).rotateAroundY(angleY).rotateAroundZ(angleZ);
    }

    /**
     * Rotates the vector from a given yaw and pitch.
     *
     * @param yawDegrees   the yaw in degrees
     * @param pitchDegrees the pitch in degrees
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public Vec rotateFromView(float yawDegrees, float pitchDegrees) {
        final double yaw = Math.toRadians(-1 * (yawDegrees + 90));
        final double pitch = Math.toRadians(-pitchDegrees);

        final double cosYaw = Math.cos(yaw);
        final double cosPitch = Math.cos(pitch);
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

    /**
     * Rotates the vector from a position's view (yaw/pitch).
     *
     * @param pos the position containing the view
     * @return a new, rotated vector
     */
    @Contract(pure = true)
    public Vec rotateFromView(Pos pos) {
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
    public Vec rotateAroundAxis(Vec axis, double angle) throws IllegalArgumentException {
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
    public Vec rotateAroundNonUnitAxis(Vec axis, double angle) throws IllegalArgumentException {
        final double x = x(), y = y(), z = z();
        final double x2 = axis.x(), y2 = axis.y(), z2 = axis.z();
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double dotProduct = this.dot(axis);

        final double newX = x2 * dotProduct * (1d - cosTheta)
                + x * cosTheta
                + (-z2 * y + y2 * z) * sinTheta;
        final double newY = y2 * dotProduct * (1d - cosTheta)
                + y * cosTheta
                + (z2 * x - x2 * z) * sinTheta;
        final double newZ = z2 * dotProduct * (1d - cosTheta)
                + z * cosTheta
                + (-y2 * x + x2 * y) * sinTheta;

        return new Vec(newX, newY, newZ);
    }

    @Override
    @Contract(pure = true, value = "_, _ -> new")
    public Vec lerp(Point point, double alpha) {
        return new Vec(x + (alpha * (point.x() - x)),
                y + (alpha * (point.y() - y)),
                z + (alpha * (point.z() - z)));
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Vec lerp(Point point, double alpha, Easing easing) {
        return (Vec) Point.super.lerp(point, alpha, easing);
    }

    /**
     * Calculates an interpolation between this vector and a target vector.
     *
     * @param target        the target vector
     * @param alpha         the alpha value, must be between 0.0 and 1.0
     * @param interpolation the interpolation function to use
     * @return the interpolated vector
     * @deprecated use {@link #lerp(Point, double, Easing)} instead
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    public Vec interpolate(Vec target, double alpha, Interpolation interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * Does nothing as this is already a {@link Vec}.
     * <p>
     * Marked as deprecated to warn against redundant usage.
     *
     * @return this vector
     */
    @Deprecated
    @Override
    @Contract(pure = true, value = "-> this")
    public Vec asVec() {
        return this;
    }

    /**
     * A functional interface representing an operation on the components of a {@link Vec}.
     */
    @FunctionalInterface
    public interface Operator {
        Operator EPSILON = operator(v -> Math.abs(v) < Vec.EPSILON ? 0 : v);
        Operator FLOOR = operator(Math::floor);
        Operator SIGNUM = operator(Math::signum);
        Operator CEIL = operator(Math::ceil);
        Operator ROUND = operator(Math::round);

        /**
         * Shortcut utility to apply the operator on all 3 components.
         *
         * @param operator the unary operator to use
         * @return the vector operator
         */
        static Operator operator(DoubleUnaryOperator operator) {
            return (x, y, z) -> new Vec(operator.applyAsDouble(x), operator.applyAsDouble(y), operator.applyAsDouble(z));
        }

        /**
         * Applies the operator to the given x, y, z components.
         *
         * @param x the x component
         * @param y the y component
         * @param z the z component
         * @return the resulting vector
         */
        Vec apply(double x, double y, double z);
    }

    /**
     * @deprecated use {@link Easing} instead
     */
    @Deprecated(forRemoval = true)
    @FunctionalInterface
    public interface Interpolation extends Easing {
        Interpolation LINEAR = a -> a;
        Interpolation SMOOTH = a -> a * a * (3 - 2 * a);

        double apply(double alpha);
    }
}
