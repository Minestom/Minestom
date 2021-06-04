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
public final class DeadHornCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9596, "facing=north", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9597, "facing=north", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9598, "facing=south", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9599, "facing=south", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9600, "facing=west", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9601, "facing=west", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9602, "facing=east", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9603, "facing=east", "waterlogged=false"));
    }
}
