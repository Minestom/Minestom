package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

public interface Vec {

    Vec ZERO = new VecImpl();

    static @NotNull Vec of(double x, double y, double z) {
        return new VecImpl(x, y, z);
    }

    @NotNull Vec with(double x, double y, double z);

    default @NotNull Vec with(@NotNull Vec vec) {
        return with(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec add(double x, double y, double z) {
        return with(x() + x, y() + y, z() + z);
    }

    default @NotNull Vec add(@NotNull Vec vec) {
        return add(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec sub(double x, double y, double z) {
        return with(x() - x, y() - y, z() - z);
    }

    default @NotNull Vec sub(@NotNull Vec vec) {
        return sub(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec mul(double x, double y, double z) {
        return with(x() * x, y() * y, z() * z);
    }

    default @NotNull Vec mul(@NotNull Vec vec) {
        return mul(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec div(double x, double y, double z) {
        return with(x() / x, y() / y, z() / z);
    }

    default @NotNull Vec div(@NotNull Vec vec) {
        return div(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec neg() {
        return with(-x(), -y(), -z());
    }

    default @NotNull Vec abs() {
        return with(Math.abs(x()), Math.abs(y()), Math.abs(z()));
    }

    default @NotNull Vec min(double x, double y, double z) {
        return with(Math.min(x(), x), Math.min(y(), y), Math.min(z(), z));
    }

    default @NotNull Vec min(@NotNull Vec vec) {
        return min(vec.x(), vec.y(), vec.z());
    }

    default @NotNull Vec max(double x, double y, double z) {
        return with(Math.max(x(), x), Math.max(y(), y), Math.max(z(), z));
    }

    default @NotNull Vec max(@NotNull Vec vec) {
        return max(vec.x(), vec.y(), vec.z());
    }

    double x();

    double y();

    double z();
}
