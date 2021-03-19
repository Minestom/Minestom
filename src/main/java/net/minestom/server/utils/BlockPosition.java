package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// TODO: pool block positions?

/**
 * Represents the position of a block, so with integers instead of floating numbers.
 */
public class BlockPosition implements PublicCloneable<BlockPosition> {

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
    public BlockPosition(double x, double y, double z) {
        final int castedY = (int) y;

        this.x = (int) Math.floor(x);
        this.y = (y == castedY) ? castedY : castedY + 1;
        this.z = (int) Math.floor(z);
    }

    /**
     * Creates a new {@link BlockPosition} from a {@link Vector}.
     *
     * @param position the position vector
     * @see #BlockPosition(double, double, double)
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
     * Gets the manhattan distance to another block position.
     *
     * @param blockPosition the block position to check the distance
     * @return the distance between 'this' and {@code blockPosition}
     */
    public int getManhattanDistance(@NotNull BlockPosition blockPosition) {
        return Math.abs(getX() - blockPosition.getX()) +
                Math.abs(getY() - blockPosition.getY()) +
                Math.abs(getZ() - blockPosition.getZ());
    }

    /**
     * Gets the distance to another block position.
     * In cases where performance matters, {@link #getDistanceSquared(BlockPosition)} should be used
     * as it does not perform the expensive Math.sqrt method.
     *
     * @param blockPosition the block position to check the distance
     * @return the distance between 'this' and {@code blockPosition}
     */
    public double getDistance(@NotNull BlockPosition blockPosition) {
        return Math.sqrt(getDistanceSquared(blockPosition));
    }

    /**
     * Gets the square distance to another block position.
     *
     * @param blockPosition the block position to check the distance
     * @return the distance between 'this' and {@code blockPosition}
     */
    public int getDistanceSquared(@NotNull BlockPosition blockPosition) {
        return MathUtils.square(getX() - blockPosition.getX()) +
                MathUtils.square(getY() - blockPosition.getY()) +
                MathUtils.square(getZ() - blockPosition.getZ());
    }

    /**
     * Copies this block position.
     *
     * @return the cloned block position
     * @deprecated use {@link #clone()}
     */
    @Deprecated
    @NotNull
    public BlockPosition copy() {
        return clone();
    }

    @NotNull
    @Override
    public BlockPosition clone() {
        try {
            return (BlockPosition) super.clone();
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    /**
     * Sets the x/y/z fields of this block position to the value of {@code block position}.
     *
     * @param blockPosition the block position to copy the values from
     */
    public void copyCoordinates(@NotNull BlockPosition blockPosition) {
        this.x = blockPosition.getX();
        this.y = blockPosition.getY();
        this.z = blockPosition.getZ();
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

    /**
     * Gets BlockPosition relative to a {@link BlockFace}
     *
     * @param face The blockface touching the relative block
     * @return The BlockPositon touching the provided blockface
     */
    @NotNull
    public BlockPosition getRelative(BlockFace face) {
        switch (face) {
            case BOTTOM:
                return new BlockPosition(getX(), getY() - 1, getZ());
            case TOP:
                return new BlockPosition(getX(), getY() + 1, getZ());
            case NORTH:
                return new BlockPosition(getX(), getY(), getZ() - 1);
            case SOUTH:
                return new BlockPosition(getX(), getY(), getZ() + 1);
            case WEST:
                return new BlockPosition(getX() - 1, getY(), getZ());
            case EAST:
                return new BlockPosition(getX() + 1, getY(), getZ());
        }
        return new BlockPosition(getX(), getY(), getZ()); // should never be called
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
