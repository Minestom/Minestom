package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;

/**
 * @author Jenya705
 */
public enum EntityFacing {

    NORTH(new Vec(0, 0, -1)),
    EAST(new Vec(1, 0, 0)),
    SOUTH(new Vec(0, 0, 1)),
    WEST(new Vec(-1, 0, 0));

    private final Vec direction;

    EntityFacing(Vec direction) {
        this.direction = direction;
    }

    public Vec getDirection() {
        return direction;
    }

    public EntityFacing opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    public EntityFacing onRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public EntityFacing onLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case EAST -> NORTH;
            case SOUTH -> EAST;
            case WEST -> SOUTH;
        };
    }

    public static EntityFacing fromYaw(float yaw) {
        float fixedYaw = yaw % 360;
        if (fixedYaw < 0) fixedYaw += 360;
        if (fixedYaw < 45) return SOUTH;
        if (fixedYaw < 135) return WEST;
        if (fixedYaw < 225) return NORTH;
        if (fixedYaw < 315) return EAST;
        return SOUTH;
    }

}
