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
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9818, "facing=north", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9819, "facing=north", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9820, "facing=south", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9821, "facing=south", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9822, "facing=west", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9823, "facing=west", "waterlogged=false"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9824, "facing=east", "waterlogged=true"));
        Block.DEAD_BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9825, "facing=east", "waterlogged=false"));
    }
}
