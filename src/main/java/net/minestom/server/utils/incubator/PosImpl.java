package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

class PosImpl implements Pos {
    private final double x, y, z;
    private final float yaw, pitch;

    PosImpl(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    PosImpl(double x, double y, double z) {
        this(x, y, z, 0, 0);
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
    public @NotNull Pos withCoord(double x, double y, double z) {
        return new PosImpl(x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Pos withView(float yaw, float pitch) {
        return new PosImpl(x, y, z, yaw, pitch);
    }

    @Override
    public float yaw() {
        return yaw;
    }

    @Override
    public float pitch() {
        return pitch;
    }
}
