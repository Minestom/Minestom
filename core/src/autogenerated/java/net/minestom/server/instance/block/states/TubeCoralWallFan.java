package net.minestom.server.instance.block.states;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;

/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(
        since = "forever",
        forRemoval = false
)
public final class TubeCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9604, "facing=north", "waterlogged=true"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9605, "facing=north", "waterlogged=false"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9606, "facing=south", "waterlogged=true"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9607, "facing=south", "waterlogged=false"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9608, "facing=west", "waterlogged=true"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9609, "facing=west", "waterlogged=false"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9610, "facing=east", "waterlogged=true"));
        Block.TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9611, "facing=east", "waterlogged=false"));
    }
}
