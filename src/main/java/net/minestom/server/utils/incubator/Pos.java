package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

public class Pos implements Coordinate {
    private final double x, y, z;
    private final float yaw, pitch;

    public Pos(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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
}
