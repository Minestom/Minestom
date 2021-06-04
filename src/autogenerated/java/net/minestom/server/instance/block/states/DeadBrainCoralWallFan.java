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
public final class DeadBrainCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9572, "facing=north", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9573, "facing=north", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9574, "facing=south", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9575, "facing=south", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9576, "facing=west", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9577, "facing=west", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9578, "facing=east", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9579, "facing=east", "waterlogged=false"));
    }
}
