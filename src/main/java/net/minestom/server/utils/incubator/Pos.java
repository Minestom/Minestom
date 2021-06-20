package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Pos implements Vec {
    public static final Pos ZERO = new Pos();

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

    public Pos() {
        this(0, 0, 0, 0, 0);
    }

    @Contract(pure = true)
    public @NotNull Pos withYaw(float yaw) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withPitch(float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Pos with(double x, double y, double z) {
        return new Pos(x, y, z, pitch, yaw);
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

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    @Override
    public @NotNull Pos asPosition() {
        return this;
    }
}
