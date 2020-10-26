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
public final class DeadTubeCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9564, "facing=north", "waterlogged=true"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9565, "facing=north", "waterlogged=false"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9566, "facing=south", "waterlogged=true"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9567, "facing=south", "waterlogged=false"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9568, "facing=west", "waterlogged=true"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9569, "facing=west", "waterlogged=false"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9570, "facing=east", "waterlogged=true"));
        Block.DEAD_TUBE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9571, "facing=east", "waterlogged=false"));
    }
}
