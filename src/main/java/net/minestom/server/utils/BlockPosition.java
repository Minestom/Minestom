package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// TODO: pool block positions?

/**
 * Represents the position of a block, so with integers instead of floating numbers.
 */
public class BlockPosition {

    private int x, y, z;

    /**
     * Creates a new {@link BlockPosition}.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     */
    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new {@link BlockPosition}.
     * <p>
     * Float positions are converted to block position, notably used by {@link Position#toBlockPosition()}.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     */
    public BlockPosition(float x, float y, float z) {
        final int castedY = (int) y;

        this.x = (int) Math.floor(x);
        this.y = (y == castedY) ? castedY : castedY + 1;
        this.z = (int) Math.floor(z);
    }

    /**
     * Creates a new {@link BlockPosition} from a {@link Vector}.
     *
     * @param position the position vector
     * @see #BlockPosition(float, float, float)
     */
    public BlockPosition(@NotNull Vector position) {
        this(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Adds offsets to this block position.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the instance of this block position
     */
    @NotNull
    public BlockPosition add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Subtracts offsets to this block position.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the instance of this block position
     */
    @NotNull
    public BlockPosition subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Adds offsets to this block position.
     *
     * @param pos the pos to add
     * @return the instance of this block position
     */
    @NotNull
    public BlockPosition add(@NotNull BlockPosition pos) {
        this.x += pos.getX();
        this.y += pos.getY();
        this.z += pos.getZ();
        return this;
    }

    /**
     * Subtracts offsets to this block position.
     *
     * @param pos the pos to subtract
     * @return the instance of this block position
     */
    @NotNull
    public BlockPosition subtract(@NotNull BlockPosition pos) {
        this.x -= pos.getX();
        this.y -= pos.getY();
        this.z -= pos.getZ();
        return this;
    }

    /**
     * Gets the block X.
     *
     * @return the block X
     */
    public int getX() {
        return x;
    }

    /**
     * Changes the X field.
     * <p>
     * WARNING: this will not change the block position.
     *
     * @param x the new X field
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the block Y.
     *
     * @return the block Y
     */
    public int getY() {
        return y;
    }

    /**
     * Changes the Y field.
     * <p>
     * WARNING: this will not change the block position.
     *
     * @param y the new Y field
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Gets the block Z.
     *
     * @return the block Z
     */
    public int getZ() {
        return z;
    }

    /**
     * Changes the Z field.
     * <p>
     * WARNING: this will not change the block position.
     *
     * @param z the new Z field
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * Gets the distance to another block position.
     *
     * @param blockPosition the block position to check the distance
     * @return the distance between 'this' and {@code blockPosition}
     */
    public int getDistance(@NotNull BlockPosition blockPosition) {
        return Math.abs(getX() - blockPosition.getX()) +
                Math.abs(getY() - blockPosition.getY()) +
                Math.abs(getZ() - blockPosition.getZ());
    }

    /**
     * Clones this block position.
     *
     * @return the cloned block position
     */
    @NotNull
    public BlockPosition clone() {
        return new BlockPosition(x, y, z);
    }

    /**
     * Converts this block position to a {@link Position}.
     *
     * @return the converted {@link Position}
     */
    @NotNull
    public Position toPosition() {
        return new Position(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPosition that = (BlockPosition) o;
        return x == that.x &&
                y == that.y &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPosition[" + x + ":" + y + ":" + z + "]";
    }
}
