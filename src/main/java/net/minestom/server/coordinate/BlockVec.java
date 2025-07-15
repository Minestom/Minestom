package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Contract;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents an immutable block position.
 * <p>
 * Usage note: If you accept a block position as an argument to a method,
 * it's usually better to accept a Point rather than a BlockVec to avoid
 * callers continually having to convert.
 */
public record BlockVec(double x, double y, double z) implements Point {
    public BlockVec {
        x = Math.floor(x);
        y = Math.floor(y);
        z = Math.floor(z);
    }

    public BlockVec(int x, int y, int z) {
        this((double) x, (double) y, (double) z);
    }

    public BlockVec(Point point) {
        this(point.x(), point.y(), point.z());
    }

    @Override
    public int blockX() {
        return (int) x;
    }

    @Override
    public int blockY() {
        return (int) y;
    }

    @Override
    public int blockZ() {
        return (int) z;
    }

    @Override
    public Point withX(DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(x), y, z);
    }

    @Override
    public Point withX(double x) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public BlockVec withBlockX(int x) {
        return new BlockVec(x, y, z);
    }

    @Override
    public Point withY(DoubleUnaryOperator operator) {
        return new Vec(x, operator.applyAsDouble(y), z);
    }

    @Override
    public Point withY(double y) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public BlockVec withBlockY(int y) {
        return new BlockVec(x, y, z);
    }

    @Override
    public Point withZ(DoubleUnaryOperator operator) {
        return new Vec(x, y, operator.applyAsDouble(z));
    }

    @Override
    public Point withZ(double z) {
        return new Vec(x, y, z);
    }

    @Contract(pure = true)
    public BlockVec withBlockZ(int z) {
        return new BlockVec(x, y, z);
    }

    @Override
    public Point add(double x, double y, double z) {
        return new Vec(this.x + x, this.y + y, this.z + z);
    }

    @Contract(pure = true)
    public BlockVec add(int x, int y, int z) {
        return new BlockVec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public Point add(Point point) {
        return new Vec(this.x + point.x(), this.y + point.y(), this.z + point.z());
    }

    @Contract(pure = true)
    public BlockVec add(BlockVec blockVec) {
        return new BlockVec(this.x + blockVec.x(), this.y + blockVec.y(), this.z + blockVec.z());
    }

    @Override
    public Point add(double value) {
        return add(value, value, value);
    }

    @Contract(pure = true)
    public BlockVec add(int value) {
        return new BlockVec(x + value, y + value, z + value);
    }

    @Override
    public Point sub(double x, double y, double z) {
        return new Vec(this.x - x, this.y - y, this.z - z);
    }

    @Contract(pure = true)
    public BlockVec sub(int x, int y, int z) {
        return new BlockVec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public Point sub(Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Contract(pure = true)
    public BlockVec sub(BlockVec blockVec) {
        return new BlockVec(this.x - blockVec.x(), this.y - blockVec.y(), this.z - blockVec.z());
    }

    @Override
    public Point sub(double value) {
        return sub(value, value, value);
    }

    @Contract(pure = true)
    public BlockVec sub(int value) {
        return new BlockVec(x - value, y - value, z - value);
    }

    @Override
    public Point mul(double x, double y, double z) {
        return new Vec(this.x * x, this.y * y, this.z * z);
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
        return new Vec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public Point div(Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public Point div(double value) {
        return div(value, value, value);
    }

    @Override
    @Contract(pure = true)
    public BlockVec relative(BlockFace face) {
        return switch (face) {
            case BOTTOM -> sub(0, 1, 0);
            case TOP -> add(0, 1, 0);
            case NORTH -> sub(0, 0, 1);
            case SOUTH -> add(0, 0, 1);
            case WEST -> sub(1, 0, 0);
            case EAST -> add(1, 0, 0);
        };
    }

    @Contract(pure = true)
    public Vec asVec() {
        return new Vec(x, y, z);
    }
}
