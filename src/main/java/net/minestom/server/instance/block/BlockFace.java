package net.minestom.server.instance.block;

import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

public enum BlockFace {
    BOTTOM(Direction.DOWN),
    TOP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST);

    private final Direction direction;

    BlockFace(Direction direction) {
        this.direction = direction;
    }

    /**
     * Gets this BlockFace's corresponding {@link Direction}.
     *
     * @return the direction that represents this BlockFace
     */
    public Direction toDirection() {
        return direction;
    }

    /**
     * Shorthand for {@link Direction#opposite()}.
     *
     * @return the opposite BlockFace
     */
    public BlockFace getOppositeFace() {
        return fromDirection(direction.opposite());
    }

    /**
     * Determines if this BlockFace is similar to another BlockFace.
     * Two BlockFaces are considered similar if they are either the same
     * or opposites of each other.
     *
     * @param other the other BlockFace to compare
     * @return true if this BlockFace is similar to the other, false otherwise
     */
    public boolean isSimilar(BlockFace other) {
        return this == other || this == other.getOppositeFace();
    }

    /**
     * Gets the horizontal BlockFace from the given yaw angle
     *
     * @param yaw the yaw angle
     * @return a horizontal BlockFace
     */
    public static BlockFace fromYaw(float yaw) {
        float degrees = (yaw - 90) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        if (0 <= degrees && degrees < 45) {
            return BlockFace.WEST;
        } else if (45 <= degrees && degrees < 135) {
            return BlockFace.NORTH;
        } else if (135 <= degrees && degrees < 225) {
            return BlockFace.EAST;
        } else if (225 <= degrees && degrees < 315) {
            return BlockFace.SOUTH;
        } else { // 315 <= degrees && degrees < 360
            return BlockFace.WEST;
        }
    }

    /**
     * Get the BlockFace corresponding to the given {@link Direction}.
     *
     * @param direction the direction
     * @return the corresponding BlockFace
     */
    public static BlockFace fromDirection(Direction direction) {
        return switch (direction) {
            case UP -> TOP;
            case DOWN -> BOTTOM;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    /**
     * Shorthand for {@link Direction#add(Direction)}.
     * Only works for horizontal faces; returns null for TOP/BOTTOM or invalid inputs.
     *
     * @param other the BlockFace to add
     * @return the rotated horizontal BlockFace, or null if invalid
     */
    public @Nullable BlockFace add(BlockFace other) {
        Direction newDirection = direction.add(other.direction);
        return newDirection != null ? fromDirection(newDirection) : null;
    }

    /**
     * Shorthand for {@link Direction#subtract(Direction)}.
     * Only works for horizontal faces; returns null for TOP/BOTTOM or invalid inputs.
     *
     * @param other the BlockFace to subtract
     * @return the resulting horizontal BlockFace, or null if invalid
     */
    public @Nullable BlockFace subtract(BlockFace other) {
        Direction newDirection = direction.subtract(other.direction);
        return newDirection != null ? fromDirection(newDirection) : null;
    }
}
