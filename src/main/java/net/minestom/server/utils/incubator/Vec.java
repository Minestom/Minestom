package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

public interface Vec {
    Vec ZERO = new VecImpl(0);
    Vec ONE = new VecImpl(1);

    @Contract(pure = true)
    static @NotNull Vec vec(double x, double y, double z) {
        return new VecImpl(x, y, z);
    }

    @Contract(pure = true)
    static @NotNull Vec vec(double value) {
        return new VecImpl(value);
    }

    @Contract(pure = true)
    @NotNull Vec with(double x, double y, double z);

    @Contract(pure = true)
    default @NotNull Vec withX(@NotNull DoubleUnaryOperator operator) {
        return with(operator.applyAsDouble(x()), y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withX(double x) {
        return with(x, y(), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withY(@NotNull DoubleUnaryOperator operator) {
        return with(x(), operator.applyAsDouble(y()), z());
    }

    @Contract(pure = true)
    default @NotNull Vec withY(double y) {
        return with(x(), y, z());
    }

    @Contract(pure = true)
    default @NotNull Vec withZ(@NotNull DoubleUnaryOperator operator) {
        return with(x(), y(), operator.applyAsDouble(z()));
    }

    @Contract(pure = true)
    default @NotNull Vec withZ(double z) {
        return with(x(), y(), z);
    }

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
    default @NotNull Vec asBlockPosition() {
        final int castedY = (int) y();
        return with((int) Math.floor(x()),
                (y() == castedY) ? castedY : castedY + 1,
                (int) Math.floor(z()));
    }

    @Contract(pure = true)
    double x();

    @Contract(pure = true)
    double y();

    @Contract(pure = true)
    double z();
}
