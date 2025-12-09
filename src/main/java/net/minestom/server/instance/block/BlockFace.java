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

    public Direction toDirection() {
        return direction;
    }

    public BlockFace getOppositeFace() {
        return switch (this) {
            case BOTTOM -> TOP;
            case TOP -> BOTTOM;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

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
     * Gets the relative BlockFace by rotating the given direction around the reference face.
     * Only works for horizontal faces; returns null for TOP/BOTTOM or invalid inputs.
     *
     * @param reference the reference BlockFace (must be horizontal)
     * @param direction the direction to rotate (must be horizontal)
     * @return the rotated BlockFace, or null if invalid
     */
    public static @Nullable BlockFace relative(BlockFace reference, BlockFace direction) {
        if (reference == TOP || reference == BOTTOM || direction == TOP || direction == BOTTOM) {
            return null;
        }

        if (reference == NORTH) return direction;

        BlockFace[] horizontals = {NORTH, EAST, SOUTH, WEST};
        int refIndex = -1, dirIndex = -1;
        for (int i = 0; i < horizontals.length; i++) {
            if (horizontals[i] == reference) refIndex = i;
            if (horizontals[i] == direction) dirIndex = i;
        }
        if (refIndex == -1 || dirIndex == -1) return null;

        int resultIndex = (refIndex + dirIndex) % 4;
        return horizontals[resultIndex];
    }
}
