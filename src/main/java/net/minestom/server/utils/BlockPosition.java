package net.minestom.server.utils;

import java.util.Objects;

// TODO: pool block positions?
public class BlockPosition {

    private int x, y, z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(float x, float y, float z) {
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
        this.z = (int) Math.floor(z);
    }

    public BlockPosition(Vector position) {
        this(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Add offsets to this block position
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the instance of this block position
     */
    public BlockPosition add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Subtract offsets to this block position
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the instance of this block position
     */
    public BlockPosition subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Add offsets to this block position
     *
     * @param pos the pos to add
     * @return the instance of this block position
     */
    public BlockPosition add(BlockPosition pos) {
        this.x += pos.getX();
        this.y += pos.getY();
        this.z += pos.getZ();
        return this;
    }

    /**
     * Subtract offsets to this block position
     *
     * @param pos the pos to subtract
     * @return the instance of this block position
     */
    public BlockPosition subtract(BlockPosition pos) {
        this.x -= pos.getX();
        this.y -= pos.getY();
        this.z -= pos.getZ();
        return this;
    }

    /**
     * Get the block X
     *
     * @return the block X
     */
    public int getX() {
        return x;
    }

    /**
     * Change the X field
     * <p>
     * WARNING: this will not change the block position
     *
     * @param x the new X field
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the block Y
     *
     * @return the block Y
     */
    public int getY() {
        return y;
    }

    /**
     * Change the Y field
     * <p>
     * WARNING: this will not change the block position
     *
     * @param y the new Y field
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get the block Z
     *
     * @return the block Z
     */
    public int getZ() {
        return z;
    }

    /**
     * Change the Z field
     * <p>
     * WARNING: this will not change the block position
     *
     * @param z the new Z field
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * Get the distance to another block position
     *
     * @param blockPosition the block position to check the distance
     * @return the distance between 'this' and {@code blockPosition}
     */
    public int getDistance(BlockPosition blockPosition) {
        return Math.abs(getX() - blockPosition.getX()) +
                Math.abs(getY() - blockPosition.getY()) +
                Math.abs(getZ() - blockPosition.getZ());
    }

    /**
     * Clone this block position
     *
     * @return the cloned block position
     */
    public BlockPosition clone() {
        return new BlockPosition(x, y, z);
    }

    /**
     * Convert this block position to a {@link Position}
     *
     * @return the converted {@link Position}
     */
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
