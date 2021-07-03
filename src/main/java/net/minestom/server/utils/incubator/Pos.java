package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Pos implements Point {
    private final double x, y, z;
    private final float yaw, pitch;

    private Pos(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Contract(pure = true)
    static @NotNull Pos pos(double x, double y, double z, float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(@NotNull Vec vec, float yaw, float pitch) {
        return pos(vec.x(), vec.y(), vec.z(), yaw, pitch);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(double x, double y, double z) {
        return new Pos(x, y, z, 0, 0);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(@NotNull Vec vec) {
        return pos(vec.x(), vec.y(), vec.z());
    }

    @Contract(pure = true)
    public @NotNull Pos withCoord(double x, double y, double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withCoord(@NotNull Vec vec) {
        return withCoord(vec.x(), vec.y(), vec.z());
    }

    @Contract(pure = true)
    public @NotNull Pos withView(float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
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
        return Vec.vec(x(), y(), z());
    }
}
