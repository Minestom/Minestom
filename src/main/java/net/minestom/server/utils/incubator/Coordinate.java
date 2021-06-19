package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

public interface Coordinate {
    @NotNull Coordinate with(double x, double y, double z);

    default @NotNull Coordinate with(@NotNull Coordinate coordinate) {
        return with(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate add(double x, double y, double z) {
        return with(x() + x, y() + y, z() + z);
    }

    default @NotNull Coordinate add(@NotNull Coordinate coordinate) {
        return add(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate sub(double x, double y, double z) {
        return with(x() - x, y() - y, z() - z);
    }

    default @NotNull Coordinate sub(@NotNull Coordinate coordinate) {
        return sub(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate mul(double x, double y, double z) {
        return with(x() * x, y() * y, z() * z);
    }

    default @NotNull Coordinate mul(@NotNull Coordinate coordinate) {
        return mul(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate div(double x, double y, double z) {
        return with(x() / x, y() / y, z() / z);
    }

    default @NotNull Coordinate div(@NotNull Coordinate coordinate) {
        return div(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate neg() {
        return with(-x(), -y(), -z());
    }

    default @NotNull Coordinate abs() {
        return with(Math.abs(x()), Math.abs(y()), Math.abs(z()));
    }

    default @NotNull Coordinate min(double x, double y, double z) {
        return with(Math.min(x(), x), Math.min(y(), y), Math.min(z(), z));
    }

    default @NotNull Coordinate min(@NotNull Coordinate coordinate) {
        return min(coordinate.x(), coordinate.y(), coordinate.z());
    }

    default @NotNull Coordinate max(double x, double y, double z) {
        return with(Math.max(x(), x), Math.max(y(), y), Math.max(z(), z));
    }

    default @NotNull Coordinate max(@NotNull Coordinate coordinate) {
        return max(coordinate.x(), coordinate.y(), coordinate.z());
    }

    double x();

    double y();

    double z();
}
