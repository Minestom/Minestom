package net.minestom.server.utils.incubator;

import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

public interface Vec {
    Vec ZERO = new VecImpl(0);
    Vec ONE = new VecImpl(1);

    @Contract(pure = true)
    static @NotNull Vec vec(double x, double y, double z) {
        return new VecImpl(x, y, z);
    }

    @Contract(pure = true)
    static @NotNull Vec vec(double value) {
        return new VecImpl(value);
    }

    @Contract(pure = true)
    @NotNull Vec with(double x, double y, double z);

    @Contract(pure = true)
    default @NotNull Vec with(@NotNull Operator operator) {
        return operator.apply(x(), y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withX(@NotNull DoubleUnaryOperator operator) {
        return with(operator.applyAsDouble(x()), y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withX(double x) {
        return with(x, y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withY(@NotNull DoubleUnaryOperator operator) {
        return with(x(), operator.applyAsDouble(y()), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withY(double y) {
        return with(x(), y, z());
    }

    @Contract(pure = true)
    default @NotNull Vec withZ(@NotNull DoubleUnaryOperator operator) {
        return with(x(), y(), operator.applyAsDouble(z()));
    }

    @Contract(pure = true)
    default @NotNull Vec withZ(double z) {
        return with(x(), y(), z);
    }

    @Contract(pure = true)
    default @NotNull Vec add(@NotNull Vec vec) {
        return with(x() + vec.x(), y() + vec.y(), z() + vec.z());
    }

    @Contract(pure = true)
    default @NotNull Vec sub(@NotNull Vec vec) {
        return with(x() - vec.x(), y() - vec.y(), z() - vec.z());
    }

    @Contract(pure = true)
    default @NotNull Vec mul(@NotNull Vec vec) {
        return with(x() * vec.x(), y() * vec.y(), z() * vec.z());
    }

    @Contract(pure = true)
    default @NotNull Vec div(@NotNull Vec vec) {
        return with(x() / vec.x(), y() / vec.y(), z() / vec.z());
    }

    @Contract(pure = true)
    default @NotNull Vec neg() {
        return with(-x(), -y(), -z());
    }

    @Contract(pure = true)
    default @NotNull Vec abs() {
        return with(Math.abs(x()), Math.abs(y()), Math.abs(z()));
    }

    @Contract(pure = true)
    default @NotNull Vec min(@NotNull Vec vec) {
        return with(Math.min(x(), vec.x()), Math.min(y(), vec.y()), Math.min(z(), vec.z()));
    }

    @Contract(pure = true)
    default @NotNull Vec max(@NotNull Vec vec) {
        return with(Math.max(x(), vec.x()), Math.max(y(), vec.y()), Math.max(z(), vec.z()));
    }

    @Contract(pure = true)
    default Vec apply(@NotNull UnaryOperator<@NotNull Vec> operator) {
        return operator.apply(this);
    }

    @Contract(pure = true)
    default @NotNull Pos asPosition() {
        return new Pos(x(), y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec asBlockPosition() {
        final int castedY = (int) y();
        return with((int) Math.floor(x()),
                (y() == castedY) ? castedY : castedY + 1,
                (int) Math.floor(z()));
    }

    @Contract(pure = true)
    double x();

    @Contract(pure = true)
    double y();

    @Contract(pure = true)
    double z();

    /**
     * Gets the magnitude of the vector, defined as sqrt(x^2+y^2+z^2). The
     * value of this method is not cached and uses a costly square-root
     * function, so do not repeatedly call this method to get the vector's
     * magnitude. NaN will be returned if the inner result of the sqrt()
     * function overflows, which will be caused if the length is too long.
     *
     * @return the magnitude
     */
    default double length() {
        return Math.sqrt(MathUtils.square(x()) + MathUtils.square(y()) + MathUtils.square(z()));
    }

    /**
     * Converts this vector to a unit vector (a vector with length of 1).
     *
     * @return the same vector
     */
    default @NotNull Vec normalize() {
        final double length = length();
        return with(x() / length, y() / length, z() / length);
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
    default double distance(@NotNull Vec vec) {
        return Math.sqrt(MathUtils.square(x() - vec.x()) +
                MathUtils.square(y() - vec.y()) +
                MathUtils.square(z() - vec.z()));
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param vec the other vector
     * @return the squared distance
     */
    default double distanceSquared(@NotNull Vec vec) {
        return MathUtils.square(x() - vec.x()) +
                MathUtils.square(y() - vec.y()) +
                MathUtils.square(z() - vec.z());
    }

    /**
     * Gets the angle between this vector and another in radians.
     *
     * @param vec the other vector
     * @return angle in radians
     */
    default double angle(@NotNull Vec vec) {
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
    default double dot(@NotNull Vec vec) {
        return x() * vec.x() + y() * vec.y() + z() * vec.z();
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
    default @NotNull Vec crossProduct(@NotNull Vec o) {
        return with(y() * o.z() - o.y() * z(),
                z() * o.x() - o.z() * x(),
                x() * o.y() - o.x() * y());
    }

    @FunctionalInterface
    interface Operator {
        @NotNull Vec apply(double x, double y, double z);
    }
}
