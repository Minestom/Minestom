package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents a position containing coordinates and a view.
 * <p>
 * To become a value then primitive type.
 */
public record Pos(double x, double y, double z, float yaw, float pitch) implements Point {
    public static final Pos ZERO = new Pos(0, 0, 0);

    public Pos {
        yaw = fixYaw(yaw);
    }

    public Pos(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Pos(Point point, float yaw, float pitch) {
        this(point.x(), point.y(), point.z(), yaw, pitch);
    }

    public Pos(Point point) {
        this(point, 0, 0);
    }

    /**
     * Converts a {@link Point} into a {@link Pos}.
     * Will cast if possible, or instantiate a new object.
     *
     * @param point the point to convert
     * @return the converted position
     */
    public static Pos fromPoint(Point point) {
        if (point instanceof Pos pos) return pos;
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
    public Pos withCoord(double x, double y, double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public Pos withCoord(Point point) {
        return withCoord(point.x(), point.y(), point.z());
    }

    @Contract(pure = true)
    public Pos withView(float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public Pos withView(Pos pos) {
        return withView(pos.yaw(), pos.pitch());
    }

    /**
     * Sets the yaw and pitch to point
     * in the direction of the point.
     */
    @Contract(pure = true)
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
        final double xz = Math.sqrt(MathUtils.square(x) + MathUtils.square(z));
        final double _2PI = 2 * Math.PI;
        return withView((float) Math.toDegrees((theta + _2PI) % _2PI),
                (float) Math.toDegrees(Math.atan(-point.y() / xz)));
    }

    @Contract(pure = true)
    public Pos withYaw(float yaw) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public Pos withYaw(DoubleUnaryOperator operator) {
        return withYaw((float) operator.applyAsDouble(yaw));
    }

    @Contract(pure = true)
    public Pos withPitch(float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public Pos withLookAt(Point point) {
        if (samePoint(point)) return this;
        final Vec delta = Vec.fromPoint(point.sub(this)).normalize();
        return withView(PositionUtils.getLookYaw(delta.x(), delta.z()),
                PositionUtils.getLookPitch(delta.x(), delta.y(), delta.z()));
    }

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

    public boolean sameView(float yaw, float pitch) {
        return Float.compare(this.yaw, yaw) == 0 &&
                Float.compare(this.pitch, pitch) == 0;
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
     * @return The closest direction {@link #yaw() yaw} and {@link #pitch() pitch} are facing to.
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
    @Contract(pure = true)
    public Pos apply(Operator operator) {
        return operator.apply(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withX(DoubleUnaryOperator operator) {
        return new Pos(operator.applyAsDouble(x), y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withX(double x) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withY(DoubleUnaryOperator operator) {
        return new Pos(x, operator.applyAsDouble(y), z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withY(double y) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withZ(DoubleUnaryOperator operator) {
        return new Pos(x, y, operator.applyAsDouble(z), yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public Pos withZ(double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    public Pos add(double x, double y, double z) {
        return new Pos(this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    @Override
    public Pos add(Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    public Pos add(double value) {
        return add(value, value, value);
    }

    @Override
    public Pos sub(double x, double y, double z) {
        return new Pos(this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    @Override
    public Pos sub(Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    public Pos sub(double value) {
        return sub(value, value, value);
    }

    @Override
    public Pos mul(double x, double y, double z) {
        return new Pos(this.x * x, this.y * y, this.z * z, yaw, pitch);
    }

    @Override
    public Pos mul(Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public Pos mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public Pos div(double x, double y, double z) {
        return new Pos(this.x / x, this.y / y, this.z / z, yaw, pitch);
    }

    @Override
    public Pos div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public Pos div(double value) {
        return div(value, value, value);
    }

    @Override
    public Pos relative(BlockFace face) {
        return (Pos) Point.super.relative(face);
    }

    @Contract(pure = true)
    public Vec asVec() {
        return new Vec(x, y, z);
    }

    @FunctionalInterface
    public interface Operator {
        Pos apply(double x, double y, double z, float yaw, float pitch);
    }

    /**
     * Fixes a yaw value that is not between -180.0F and 180.0F
     * So for example -1355.0F becomes 85.0F and 225.0F becomes -135.0F
     *
     * @param yaw The possible "wrong" yaw
     * @return a fixed yaw
     */
    private static float fixYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < -180.0F) {
            yaw += 360.0F;
        } else if (yaw > 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }
}
