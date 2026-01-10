package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents a 3D position with double-precision coordinates and viewing direction.
 * <p>
 * Combines {@link Vec} with yaw and pitch angles, making it suitable for entities
 * and cameras that need both location and orientation.
 * <p>
 * View angles are automatically normalized.
 *
 * @param x     the X coordinate
 * @param y     the Y coordinate
 * @param z     the Z coordinate
 * @param yaw   the yaw (rotation around vertical axis) in degrees (-180, 180]
 * @param pitch the pitch (rotation around lateral axis) in degrees [-90, 90]
 */
public record Pos(double x, double y, double z, float yaw, float pitch) implements Point {
    public static final Pos ZERO = new Pos(0, 0, 0);

    /**
     * The epsilon used to compare two views (yaw/pitch) if applicable.
     */
    public static final float VIEW_EPSILON = 1e-4f;

    public Pos {
        yaw = fixYaw(yaw);
        pitch = fixPitch(pitch);
    }

    /**
     * Creates a position with the given coordinates (x/y/z) and default view (yaw/pitch = 0).
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Pos(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * Creates a position from a point with the given view (yaw/pitch).
     *
     * @param point the point containing the coordinates (x/y/z)
     * @param yaw   the yaw
     * @param pitch the pitch
     * @deprecated Use {@link Point#asPos()} instead with {@link #withView(float, float)}
     */
    @Deprecated
    public Pos(Point point, float yaw, float pitch) {
        this(point.x(), point.y(), point.z(), yaw, pitch);
    }

    /**
     * Creates a position from a point with the default view (yaw/pitch = 0).
     *
     * @param point the point containing the coordinates (x/y/z)
     * @deprecated Use {@link Point#asPos()} instead
     */
    @Deprecated
    public Pos(Point point) {
        this(point, 0, 0);
    }

    /**
     * Converts a {@link Point} into a {@link Pos}.
     * Will cast if possible, or instantiate a new object.
     *
     * @param point the point to convert
     * @return the converted position
     * @deprecated use {@link Point#asPos()} instead
     */
    @Deprecated(forRemoval = true)
    public static Pos fromPoint(Point point) {
        if (point instanceof Pos pos) return pos;
        return new Pos(point.x(), point.y(), point.z());
    }

    /**
     * Fixes a pitch value that is not between -90.0f and 90.0f
     * So for example, -135.0f becomes -90.0f and 225.0f becomes 90.0f
     *
     * @param pitch The possible "wrong" pitch
     * @return a fixed pitch in the range [-90.0f, 90.0f]
     */
    public static float fixPitch(float pitch) {
        return Math.clamp(pitch, -90.0f, 90.0f);
    }

    /**
     * Fixes a yaw value that is not between -180.0f (exclusive) and 180.0f (inclusive).
     * Wraps the yaw to the nearest equivalent angle in this range.
     * For example, -1355.0f becomes 85.0f and 225.0f becomes -135.0f.
     *
     * @param yaw The possible "wrong" yaw
     * @return a fixed yaw in the range (-180.0f, 180.0f]
     */
    public static float fixYaw(float yaw) {
        return yaw - 360.0f * (float) Math.ceil((yaw - 180.0f) / 360.0f);
    }

    /**
     * Changes the 3 coordinates of this position (x/y/z).
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withCoord(double x, double y, double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    /**
     * Changes the coordinates to match the provided point.
     *
     * @param point the point to use for coordinates (x/y/z)
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withCoord(Point point) {
        return withCoord(point.x(), point.y(), point.z());
    }

    /**
     * Changes the view of this position (yaw/pitch).
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withView(float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    /**
     * Changes the view to match the provided position.
     *
     * @param pos the position to use for the view (yaw/pitch)
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withView(Pos pos) {
        return withView(pos.yaw(), pos.pitch());
    }

    /**
     * Sets the yaw and pitch to point
     * in the direction of the point.
     *
     * @param point the point to look at
     * @return a new position
     */
    @Contract(pure = true, value = "_ -> new")
    public Pos withDirection(Point point) {
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
        final double xz = Math.sqrt((x * x) + (z * z));
        final double _2PI = 2 * Math.PI;
        return withView((float) Math.toDegrees((theta + _2PI) % _2PI),
                (float) Math.toDegrees(Math.atan(-point.y() / xz)));
    }

    /**
     * Changes the yaw of this position.
     *
     * @param yaw the new yaw
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withYaw(float yaw) {
        return new Pos(x, y, z, yaw, pitch);
    }

    /**
     * Applies an operator to the yaw of this position.
     *
     * @param operator the operator to apply to the yaw
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withYaw(DoubleUnaryOperator operator) {
        return withYaw((float) operator.applyAsDouble(yaw));
    }

    /**
     * Changes the pitch of this position.
     *
     * @param pitch the new pitch
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withPitch(float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    /**
     * Changes the view to look at a specific point.
     *
     * @param point the point to look at
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withLookAt(Point point) {
        if (samePoint(point)) return this;
        final Vec delta = point.sub(this).asVec().normalize();
        return withView(PositionUtils.getLookYaw(delta.x(), delta.z()),
                PositionUtils.getLookPitch(delta.x(), delta.y(), delta.z()));
    }

    /**
     * Applies an operator to the pitch of this position.
     *
     * @param operator the operator to apply to the pitch
     * @return a new position
     */
    @Contract(pure = true)
    public Pos withPitch(DoubleUnaryOperator operator) {
        return withPitch((float) operator.applyAsDouble(pitch));
    }

    /**
     * Checks if two positions have a similar view (yaw/pitch).
     *
     * @param position the position to compare
     * @return true if the two positions have the same view
     */
    public boolean sameView(Pos position) {
        return sameView(position.yaw(), position.pitch());
    }

    /**
     * Checks if the yaw and pitch are the same as the given ones.
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return true if the yaw and pitch are the same
     */
    public boolean sameView(float yaw, float pitch) {
        return Float.compare(this.yaw, yaw) == 0 &&
                Float.compare(this.pitch, pitch) == 0;
    }

    /**
     * Checks if the yaw and pitch are approximately the same as the given ones.
     *
     * @param yaw     the yaw
     * @param pitch   the pitch
     * @param epsilon the maximum difference to consider the values equal
     * @return true if the yaw and pitch are approximately the same
     */
    public boolean similarView(float yaw, float pitch, float epsilon) {
        return Math.abs(this.yaw - yaw) < epsilon &&
                Math.abs(this.pitch - pitch) < epsilon;
    }

    /**
     * Checks if two positions have approximately similar views (yaw/pitch).
     *
     * @param position the position to compare
     * @param epsilon  the maximum difference to consider the values equal
     * @return true if the two positions have a similar view
     */
    public boolean similarView(Pos position, float epsilon) {
        return similarView(position.yaw(), position.pitch(), epsilon);
    }

    /**
     * Checks if two positions have approximately similar views (yaw/pitch).
     * <p>
     * Uses {@link #VIEW_EPSILON} as epsilon.
     *
     * @param position the position to compare
     * @return true if the two positions have a similar view
     */
    public boolean similarView(Pos position) {
        return similarView(position.yaw(), position.pitch(), VIEW_EPSILON);
    }

    /**
     * Checks if the yaw and pitch are approximately the same as the given ones.
     * <p>
     * Uses {@link #VIEW_EPSILON} as epsilon.
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return true if the yaw and pitch are approximately the same
     */
    public boolean similarView(float yaw, float pitch) {
        return similarView(yaw, pitch, VIEW_EPSILON);
    }

    /**
     * Gets a unit-vector pointing in the direction that this Location is
     * facing.
     *
     * @return a vector pointing the direction of this location's {@link
     * #pitch() pitch} and {@link #yaw() yaw}
     */
    public Vec direction() {
        final float rotX = yaw;
        final float rotY = pitch;
        final double xz = Math.cos(Math.toRadians(rotY));
        return new Vec(-xz * Math.sin(Math.toRadians(rotX)),
                -Math.sin(Math.toRadians(rotY)),
                xz * Math.cos(Math.toRadians(rotX)));
    }

    /**
     * Gets the closest direction {@link #yaw()} and {@link #pitch()} are facing to.
     *
     * @return the direction this position is facing
     */
    public Direction facing() {
        if (pitch < -45) return Direction.UP;
        if (pitch > 45) return Direction.DOWN;
        if (yaw > 135 || yaw <= -135) return Direction.NORTH;
        if (-135 < yaw && yaw <= -45) return Direction.EAST;
        if (-45 < yaw && yaw <= 45) return Direction.SOUTH;
        if (45 < yaw) return Direction.WEST;
        throw new IllegalStateException("Illegal yaw (%s) or pitch (%s) value.".formatted(this.yaw, pitch));
    }

    /**
     * Returns a new position based on this position fields.
     *
     * @param operator the operator deconstructing this object and providing a new position
     * @return the new position
     */
    public Pos apply(Operator operator) {
        return operator.apply(x, y, z, yaw, pitch);
    }

    @Override
    @Contract("_ -> new")
    public Pos withX(DoubleUnaryOperator operator) {
        return new Pos(operator.applyAsDouble(x), y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos withX(double x) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract("_ -> new")
    public Pos withY(DoubleUnaryOperator operator) {
        return new Pos(x, operator.applyAsDouble(y), z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos withY(double y) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract("_ -> new")
    public Pos withZ(DoubleUnaryOperator operator) {
        return new Pos(x, y, operator.applyAsDouble(z), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos withZ(double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos add(double x, double y, double z) {
        return new Pos(this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos add(Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos add(double value) {
        return add(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos sub(double x, double y, double z) {
        return new Pos(this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos sub(Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos sub(double value) {
        return sub(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos mul(double x, double y, double z) {
        return new Pos(this.x * x, this.y * y, this.z * z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos mul(Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos mul(double value) {
        return mul(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos div(double x, double y, double z) {
        return new Pos(this.x / x, this.y / y, this.z / z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos div(double value) {
        return div(value, value, value);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos relative(BlockFace face) {
        return (Pos) Point.super.relative(face);
    }

    /**
     * Does nothing as this is already a {@link Pos}.
     * <p>
     * Marked as deprecated to warn against redundant usage.
     *
     * @return this position
     */
    @Deprecated
    @Override
    @Contract(pure = true, value = "-> this")
    public Pos asPos() {
        return this;
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Pos normalize() {
        final double length = length();
        return new Pos(x / length, y / length, z / length, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos cross(Point point) {
        return new Pos(y * point.z() - point.y() * z,
                z * point.x() - point.z() * x,
                x * point.y() - point.x() * y,
                yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_, _ -> new")
    public Pos lerp(Point point, double alpha) {
        return new Pos(x + (alpha * (point.x() - x)),
                y + (alpha * (point.y() - y)),
                z + (alpha * (point.z() - z)),
                yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos lerp(Point point, double alpha, Easing easing) {
        return (Pos) Point.super.lerp(point, alpha, easing);
    }

    /**
     * Calculates a linear interpolation between this position's view and another position's view (yaw/pitch).
     * The coordinates (x/y/z) remain unchanged.
     *
     * @param pos   the other position
     * @param alpha the alpha value, must be between 0.0 and 1.0
     * @return a new position with interpolated view
     */
    @Contract(pure = true, value = "_, _ -> new")
    public Pos lerpView(Pos pos, double alpha) {
        return new Pos(x, y, z,
                yaw + (float) (alpha * (pos.yaw() - yaw)),
                pitch + (float) (alpha * (pos.pitch() - pitch)));
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Pos neg() {
        return new Pos(-x, -y, -z, yaw, pitch);
    }

    /**
     * Negates the view (yaw/pitch) of this position.
     *
     * @return a new position
     */
    @Contract(pure = true, value = "-> new")
    public Pos negView() {
        return new Pos(x, y, z, -yaw, -pitch);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public Pos abs() {
        return new Pos(Math.abs(x), Math.abs(y), Math.abs(z), yaw, pitch);
    }

    /**
     * Returns the absolute value of the view (yaw/pitch).
     *
     * @return a new position
     */
    @Contract(pure = true, value = "-> new")
    public Pos absView() {
        return new Pos(x, y, z, Math.abs(yaw), Math.abs(pitch));
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos min(Point point) {
        return new Pos(Math.min(x, point.x()), Math.min(y, point.y()), Math.min(z, point.z()), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos min(double x, double y, double z) {
        return new Pos(Math.min(this.x, x), Math.min(this.y, y), Math.min(this.z, z), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos min(double value) {
        return new Pos(Math.min(x, value), Math.min(y, value), Math.min(z, value), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos max(Point point) {
        return new Pos(Math.max(x, point.x()), Math.max(y, point.y()), Math.max(z, point.z()), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public Pos max(double x, double y, double z) {
        return new Pos(Math.max(this.x, x), Math.max(this.y, y), Math.max(this.z, z), yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public Pos max(double value) {
        return new Pos(Math.max(x, value), Math.max(y, value), Math.max(z, value), yaw, pitch);
    }

    /**
     * A functional interface representing an operation on the components of a {@link Pos}.
     */
    @FunctionalInterface
    public interface Operator {
        /**
         * Uses a {@link Vec.Operator} for the position components (x/y/z) for a {@link Operator}.
         *
         * @param operator the vector operator
         * @return the position operator
         */
        static Operator operator(Vec.Operator operator) {
            return (x, y, z, yaw, pitch) -> operator.apply(x, y, z).asPos().withView(yaw, pitch);
        }

        /**
         * Applies this operator to the given position components.
         *
         * @param x     the x component
         * @param y     the y component
         * @param z     the z component
         * @param yaw   the yaw component
         * @param pitch the pitch component
         * @return the resulting position
         */
        Pos apply(double x, double y, double z, float yaw, float pitch);
    }
}
