package net.minestom.server.utils;

import net.minestom.server.coordinate.Vec;

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
}
