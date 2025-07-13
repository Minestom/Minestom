package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

import static net.minestom.server.coordinate.CoordConversion.globalToBlock;

/**
 * Represents an immutable block position.
 * <p>
 * Usage note: If you accept a block position as an argument to a method,
 * it's usually better to accept a Point rather than a BlockVec to avoid
 * callers continually having to convert.
 */
public record BlockVec(int blockX, int blockY, int blockZ) implements Point {
    public BlockVec(double x, double y, double z) {
        this(globalToBlock(x), globalToBlock(y), globalToBlock(z));
    }

    public BlockVec(@NotNull Point point) {
        this(point.x(), point.y(), point.z());
    }

    @Override
    public double x() {
        return blockX;
    }

    @Override
    public double y() {
        return blockY;
    }

    @Override
    public double z() {
        return blockZ;
    }

    @Override
    public @NotNull Point withX(@NotNull DoubleUnaryOperator operator) {
        return new Vec(operator.applyAsDouble(blockX), blockY, blockZ);
    }

    @Override
    public @NotNull Point withX(double x) {
        return new Vec(x, blockY, blockZ);
    }

    @Contract(pure = true)
    public @NotNull BlockVec withBlockX(int x) {
        return new BlockVec(x, blockY, blockZ);
    }

    @Override
    public @NotNull Point withY(@NotNull DoubleUnaryOperator operator) {
        return new Vec(blockX, operator.applyAsDouble(blockY), blockZ);
    }

    @Override
    public @NotNull Point withY(double y) {
        return new Vec(blockX, y, blockZ);
    }

    @Contract(pure = true)
    public @NotNull BlockVec withBlockY(int y) {
        return new BlockVec(blockX, y, blockZ);
    }

    @Override
    public @NotNull Point withZ(@NotNull DoubleUnaryOperator operator) {
        return new Vec(blockX, blockY, operator.applyAsDouble(blockZ));
    }

    @Override
    public @NotNull Point withZ(double z) {
        return new Vec(blockX, blockY, z);
    }

    @Contract(pure = true)
    public @NotNull BlockVec withBlockZ(int z) {
        return new BlockVec(blockX, blockY, z);
    }

    @Override
    public @NotNull Point add(double x, double y, double z) {
        return new Vec(blockX + x, blockY + y, blockZ + z);
    }

    @Contract(pure = true)
    public @NotNull BlockVec add(int x, int y, int z) {
        return new BlockVec(blockX + x, blockY + y, blockZ + z);
    }

    @Override
    public @NotNull Point add(@NotNull Point point) {
        return new Vec(blockX + point.x(), blockY + point.y(), blockZ + point.z());
    }

    @Contract(pure = true)
    public @NotNull BlockVec add(@NotNull BlockVec blockVec) {
        return new BlockVec(blockX + blockVec.x(), blockY + blockVec.y(), blockZ + blockVec.z());
    }

    @Override
    public @NotNull Point add(double value) {
        return add(value, value, value);
    }

    @Contract(pure = true)
    public @NotNull BlockVec add(int value) {
        return new BlockVec(blockX + value, blockY + value, blockZ + value);
    }

    @Override
    public @NotNull Point sub(double x, double y, double z) {
        return new Vec(blockX - x, blockY - y, blockZ - z);
    }

    @Contract(pure = true)
    public @NotNull BlockVec sub(int x, int y, int z) {
        return new BlockVec(blockX - x, blockY - y, blockZ - z);
    }

    @Override
    public @NotNull Point sub(@NotNull Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Contract(pure = true)
    public @NotNull BlockVec sub(@NotNull BlockVec blockVec) {
        return new BlockVec(blockX - blockVec.x(), blockY - blockVec.y(), blockZ - blockVec.z());
    }

    @Override
    public @NotNull Point sub(double value) {
        return sub(value, value, value);
    }

    @Contract(pure = true)
    public @NotNull BlockVec sub(int value) {
        return new BlockVec(blockX - value, blockY - value, blockZ - value);
    }

    @Override
    public @NotNull Point mul(double x, double y, double z) {
        return new Vec(blockX * x, blockY * y, blockZ * z);
    }

    @Override
    public @NotNull Point mul(@NotNull Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Point mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public @NotNull Point div(double x, double y, double z) {
        return new Vec(blockX / x, blockY / y, blockZ / z);
    }

    @Override
    public @NotNull Point div(@NotNull Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Point div(double value) {
        return div(value, value, value);
    }

    @Override
    @Contract(pure = true)
    public @NotNull BlockVec relative(@NotNull BlockFace face) {
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
    public @NotNull Vec asVec() {
        return new Vec(blockX, blockY, blockZ);
    }
}
