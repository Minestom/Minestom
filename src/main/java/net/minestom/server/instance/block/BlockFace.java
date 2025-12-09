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
     * Computes the rotated BlockFace by adding two horizontal BlockFaces.
     * Only works for horizontal faces; returns null for TOP/BOTTOM or invalid inputs.
     *
     * @param other the BlockFace to add
     * @return the rotated horizontal BlockFace, or null if invalid
     */
    public @Nullable BlockFace add(BlockFace other) {
        BlockFace[] horizontals = {NORTH, EAST, SOUTH, WEST};

        int aIndex = -1, bIndex = -1;
        for (int i = 0; i < horizontals.length; i++) {
            if (horizontals[i] == this) aIndex = i;
            if (horizontals[i] == other) bIndex = i;
        }
        if (aIndex == -1 || bIndex == -1) return null;

        int resultIndex = (aIndex + bIndex) % 4;
        return horizontals[resultIndex];
    }

    /**
     * Computes the BlockFace by subtracting two horizontal BlockFaces.
     * Only works for horizontal faces; returns null for TOP/BOTTOM or invalid inputs.
     *
     * @param other the BlockFace to subtract
     * @return the resulting horizontal BlockFace, or null if invalid
     */
    public @Nullable BlockFace subtract(BlockFace other) {
        BlockFace[] horizontals = {NORTH, EAST, SOUTH, WEST};

        int aIndex = -1, bIndex = -1;
        for (int i = 0; i < horizontals.length; i++) {
            if (horizontals[i] == this) aIndex = i;
            if (horizontals[i] == other) bIndex = i;
        }
        if (aIndex == -1 || bIndex == -1) return null;

        int resultIndex = (aIndex - bIndex + 4) % 4;
        return horizontals[resultIndex];
    }
}
