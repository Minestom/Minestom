package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a position containing coordinates and a view.
 * <p>
 * To become record and primitive.
 */
public final class Pos implements Point {
    public static final Pos ZERO = new Pos(0, 0, 0);

    private final double x, y, z;
    private final float yaw, pitch;

    public Pos(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Pos(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Pos(@NotNull Point point, float yaw, float pitch) {
        this(point.x(), point.y(), point.z(), yaw, pitch);
    }

    public Pos(@NotNull Point point) {
        this(point, 0, 0);
    }

    /**
     * Converts a {@link Point} into a {@link Pos}.
     * Will cast if possible, or instantiate a new object.
     *
     * @param point the point to convert
     * @return the converted position
     */
    public static @NotNull Pos fromPoint(@NotNull Point point) {
        if (point instanceof Pos)
            return (Pos) point;
        return new Pos(point.x(), point.y(), point.z());
    }

    /**
     * Changes the 3 coordinates of this position.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new position
     */
    @Contract(pure = true)
    public @NotNull Pos withCoord(double x, double y, double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withCoord(@NotNull Point point) {
        return withCoord(point.x(), point.y(), point.z());
    }

    @Contract(pure = true)
    public @NotNull Pos withView(float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    /**
     * Sets the yaw and pitch to point
     * in the direction of the point.
     */
    @Contract(pure = true)
    public @NotNull Pos withDirection(@NotNull Point point) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double x = point.x();
        final double z = point.z();
        if (x == 0 && z == 0) {
            return withPitch(point.y() > 0 ? -90f : 90f);
        }
        final double theta = Math.atan2(-x, z);
        final double xz = Math.sqrt(MathUtils.square(x) + MathUtils.square(z));
        final double _2PI = 2 * Math.PI;
        return withView((float) Math.toDegrees((theta + _2PI) % _2PI),
                (float) Math.toDegrees(Math.atan(-point.y() / xz)));
    }

    @Contract(pure = true)
    public @NotNull Pos withYaw(float yaw) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withYaw(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, y, z, (float) operator.applyAsDouble(yaw), pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withPitch(float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withPitch(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, y, z, yaw, (float) operator.applyAsDouble(pitch));
    }

    /**
     * Checks if two positions have a similar view (yaw/pitch).
     *
     * @param position the position to compare
     * @return true if the two positions have the same view
     */
    public boolean sameView(@NotNull Pos position) {
        return Float.compare(position.yaw, yaw) == 0 &&
                Float.compare(position.pitch, pitch) == 0;
    }

    /**
     * Gets a unit-vector pointing in the direction that this Location is
     * facing.
     *
     * @return a vector pointing the direction of this location's {@link
     * #pitch() pitch} and {@link #yaw() yaw}
     */
    public @NotNull Vec direction() {
        final float rotX = yaw;
        final float rotY = pitch;
        final double xz = Math.cos(Math.toRadians(rotY));
        return new Vec(-xz * Math.sin(Math.toRadians(rotX)),
                -Math.sin(Math.toRadians(rotY)),
                xz * Math.cos(Math.toRadians(rotX)));
    }

    @Override
    @Contract(pure = true)
    public double x() {
        return x;
    }

    @Override
    @Contract(pure = true)
    public double y() {
        return y;
    }

    @Override
    @Contract(pure = true)
    public double z() {
        return z;
    }

    /**
     * Returns a new position based on this position fields.
     *
     * @param operator the operator deconstructing this object and providing a new position
     * @return the new position
     */
    @Contract(pure = true)
    public @NotNull Pos apply(@NotNull Operator operator) {
        return operator.apply(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withX(@NotNull DoubleUnaryOperator operator) {
        return new Pos(operator.applyAsDouble(x), y, z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withX(double x) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withY(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, operator.applyAsDouble(y), z);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withY(double y) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withZ(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, y, operator.applyAsDouble(z));
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withZ(double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Pos add(double x, double y, double z) {
        return new Pos(this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    @Override
    public @NotNull Pos add(@NotNull Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos add(double value) {
        return add(value, value, value);
    }

    @Override
    public @NotNull Pos sub(double x, double y, double z) {
        return new Pos(this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    @Override
    public @NotNull Pos sub(@NotNull Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos sub(double value) {
        return sub(value, value, value);
    }

    @Override
    public @NotNull Pos mul(double x, double y, double z) {
        return new Pos(this.x * x, this.y * y, this.z * z, yaw, pitch);
    }

    @Override
    public @NotNull Pos mul(@NotNull Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public @NotNull Pos div(double x, double y, double z) {
        return new Pos(this.x / x, this.y / y, this.z / z, yaw, pitch);
    }

    @Override
    public @NotNull Pos div(@NotNull Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos div(double value) {
        return div(value, value, value);
    }

    @Override
    public @NotNull Pos relative(@NotNull BlockFace face) {
        return (Pos) Point.super.relative(face);
    }

    @Contract(pure = true)
    public float yaw() {
        return yaw;
    }

    @Contract(pure = true)
    public float pitch() {
        return pitch;
    }

    @Contract(pure = true)
    public @NotNull Vec asVec() {
        return new Vec(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos pos = (Pos) o;
        return Double.compare(pos.x, x) == 0 &&
                Double.compare(pos.y, y) == 0 &&
                Double.compare(pos.z, z) == 0 &&
                Float.compare(pos.yaw, yaw) == 0 &&
                Float.compare(pos.pitch, pitch) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "Pos{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }

    @FunctionalInterface
    public interface Operator {
        @NotNull Pos apply(double x, double y, double z, float yaw, float pitch);
    }
}
