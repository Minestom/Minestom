package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface Vec {

    Vec ZERO = new VecImpl();

    @Contract(pure = true)
    static @NotNull Vec vec(double x, double y, double z) {
        return new VecImpl(x, y, z);
    }

    @Contract(pure = true)
    static @NotNull Vec x(double x) {
        return vec(x, 0, 0);
    }

    @Contract(pure = true)
    static @NotNull Vec y(double y) {
        return vec(0, y, 0);
    }

    @Contract(pure = true)
    static @NotNull Vec z(double z) {
        return vec(0, 0, z);
    }

    @Contract(pure = true)
    static @NotNull Vec xz(double x, double z) {
        return vec(x, 0, z);
    }

    @Contract(pure = true)
    @NotNull Vec with(double x, double y, double z);

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
    double x();

    @Contract(pure = true)
    double y();

    @Contract(pure = true)
    double z();
}
