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
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9842, "facing=north", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9843, "facing=north", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9844, "facing=south", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9845, "facing=south", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9846, "facing=west", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9847, "facing=west", "waterlogged=false"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9848, "facing=east", "waterlogged=true"));
        Block.DEAD_HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9849, "facing=east", "waterlogged=false"));
    }
}
