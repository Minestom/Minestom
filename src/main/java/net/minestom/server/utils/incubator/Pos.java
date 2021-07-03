package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Pos extends Point {

    @Contract(pure = true)
    static @NotNull Pos pos(double x, double y, double z, float yaw, float pitch) {
        return new PosImpl(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(@NotNull Vec vec, float yaw, float pitch) {
        return pos(vec.x(), vec.y(), vec.z(), yaw, pitch);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(double x, double y, double z) {
        return new PosImpl(x, y, z);
    }

    @Contract(pure = true)
    static @NotNull Pos pos(@NotNull Vec vec) {
        return pos(vec.x(), vec.y(), vec.z());
    }

    @Contract(pure = true)
    @NotNull Pos withCoord(double x, double y, double z);

    @Contract(pure = true)
    default @NotNull Pos withCoord(@NotNull Vec vec) {
        return withCoord(vec.x(), vec.y(), vec.z());
    }

    @Contract(pure = true)
    @NotNull Pos withView(float yaw, float pitch);

    @Contract(pure = true)
    float yaw();

    @Contract(pure = true)
    float pitch();

    @Contract(pure = true)
    default @NotNull Vec asVec() {
        return Vec.vec(x(), y(), z());
    }
}
