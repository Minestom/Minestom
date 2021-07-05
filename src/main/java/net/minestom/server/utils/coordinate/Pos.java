package net.minestom.server.utils.coordinate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents a position containing coordinates and a view.
 * <p>
 * To become record and primitive.
 */
public final class Pos implements Point {
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
}
