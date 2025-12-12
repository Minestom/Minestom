package net.minestom.server.utils;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public enum Direction {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    public static final Direction[] HORIZONTAL = {SOUTH, WEST, NORTH, EAST};

    private final int normalX;
    private final int normalY;
    private final int normalZ;
    private final Vec normalVec;

    Direction(int normalX, int normalY, int normalZ) {
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.normalVec = new Vec(normalX, normalY, normalZ);
    }

    public int normalX() {
        return normalX;
    }

    public int normalY() {
        return normalY;
    }

    public int normalZ() {
        return normalZ;
    }

    public Vec vec() {
        return normalVec;
    }

    public Vec mul(double mult) {
        return normalVec.mul(mult);
    }

    public boolean positive() {
        return normalX > 0 || normalY > 0 || normalZ > 0;
    }

    public boolean negative() {
        return !positive();
    }

    public boolean vertical() {
        return this == UP || this == DOWN;
    }

    public boolean horizontal() {
        return !vertical();
    }

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }

    /**
     * Mirrors this direction across the specified horizontal axis.
     *
     * @param axis the Axis to mirror across
     * @return the mirrored Direction, the same Direction if unaffected by the axis, or null if mirroring across Y axis
     */
    public @UnknownNullability Direction mirror(Axis axis) {
        return switch (axis) {
            case X -> switch (this) {
                case EAST -> WEST;
                case WEST -> EAST;
                default -> this;
            };
            case Z -> switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                default -> this;
            };
            default -> null;
        };
    }

    private static final Direction[] HORIZONTALS = {NORTH, EAST, SOUTH, WEST};

    /**
     * Adds two horizontal directions together, treating them as rotations.
     * NORTH acts as the identity (adding NORTH returns the original direction).
     * Other directions rotate clockwise: EAST = 90°, SOUTH = 180°, WEST = 270°.
     *
     * @param other the horizontal Direction to add to this Direction
     * @return the resulting horizontal Direction, or null if either direction is not horizontal
     */
    public @UnknownNullability Direction add(Direction other) {
        int aIndex = -1, bIndex = -1;
        for (int i = 0; i < HORIZONTALS.length; i++) {
            if (HORIZONTALS[i] == this) aIndex = i;
            if (HORIZONTALS[i] == other) bIndex = i;
        }
        if (aIndex == -1 || bIndex == -1) return null;

        int resultIndex = (aIndex + bIndex) % 4;
        return HORIZONTALS[resultIndex];
    }

    /**
     * Subtracts another horizontal direction from this one, treating them as rotations.
     * NORTH acts as the identity (subtracting NORTH returns the original direction).
     * Other directions rotate counter-clockwise: EAST = -90°, SOUTH = -180°, WEST = -270°.
     *
     * @param other the horizontal Direction to subtract from this Direction
     * @return the resulting horizontal Direction, or null if either direction is not horizontal
     */
    public @UnknownNullability Direction subtract(Direction other) {
        int aIndex = -1, bIndex = -1;
        for (int i = 0; i < HORIZONTALS.length; i++) {
            if (HORIZONTALS[i] == this) aIndex = i;
            if (HORIZONTALS[i] == other) bIndex = i;
        }
        if (aIndex == -1 || bIndex == -1) return null;

        int resultIndex = (aIndex - bIndex + 4) % 4;
        return HORIZONTALS[resultIndex];
    }
}