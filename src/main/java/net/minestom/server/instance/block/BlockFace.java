package net.minestom.server.instance.block;

import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public enum BlockFace {
    BOTTOM(Direction.DOWN, "down"),
    TOP(Direction.UP, "up"),
    NORTH(Direction.NORTH, "north"),
    SOUTH(Direction.SOUTH, "south"),
    WEST(Direction.WEST, "west"),
    EAST(Direction.EAST, "east");

    private final Direction direction;
    private final String facing;

    BlockFace(Direction direction, String facing) {
        this.direction = direction;
        this.facing = facing;
    }

    public Direction toDirection() {
        return direction;
    }

    public String toFacing() { return facing; }

    public Block applyFacing(Block block) { return block.withProperty("facing", facing); }

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
}
