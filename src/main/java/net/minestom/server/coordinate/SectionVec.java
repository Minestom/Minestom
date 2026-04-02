package net.minestom.server.coordinate;

import java.util.function.DoubleUnaryOperator;

public record SectionVec(int sectionX, int sectionY, int sectionZ) implements Point {
    @Override
    public double x() {
        return blockX();
    }

    @Override
    public double y() {
        return blockY();
    }

    @Override
    public double z() {
        return blockZ();
    }

    @Override
    public int blockX() {
        return sectionX << 4;
    }

    @Override
    public int blockY() {
        return sectionY << 4;
    }

    @Override
    public int blockZ() {
        return sectionZ << 4;
    }

    @Override
    public Point withX(DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(x()), y(), z());
    }

    @Override
    public Point withX(double x) {
        return new Vec(x, y(), z());
    }

    @Override
    public Point withY(DoubleUnaryOperator operator) {
        return new Vec(x(), operator.applyAsDouble(y()), z());
    }

    @Override
    public Point withY(double y) {
        return new Vec(x(), y, z());
    }

    @Override
    public Point withZ(DoubleUnaryOperator operator) {
        return new Vec(x(), y(), operator.applyAsDouble(z()));
    }

    @Override
    public Point withZ(double z) {
        return new Vec(x(), y(), z);
    }

    @Override
    public Point add(double x, double y, double z) {
        return new Vec(x() + x, y() + y, z() + z);
    }

    @Override
    public Point add(Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    public Point add(double value) {
        return add(value, value, value);
    }

    @Override
    public Point sub(double x, double y, double z) {
        return add(-x, -y, -z);
    }

    @Override
    public Point sub(Point point) {
        return add(-point.x(), -point.y(), -point.z());
    }

    @Override
    public Point sub(double value) {
        return add(-value);
    }

    @Override
    public Point mul(double x, double y, double z) {
        return new Vec(x() * x, y() * y, z() * z);
    }

    @Override
    public Point mul(Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public Point mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public Point div(double x, double y, double z) {
        return new Vec(x() / x, y() / y, z() / z);
    }

    @Override
    public Point div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public Point div(double value) {
        return div(value, value, value);
    }
}
