package net.minestom.server.instance.block;

import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
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

    public boolean isSimilar(@NotNull BlockFace other) {
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
}
