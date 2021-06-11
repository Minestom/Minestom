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
public final class HornCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9882, "facing=north", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9883, "facing=north", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9884, "facing=south", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9885, "facing=south", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9886, "facing=west", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9887, "facing=west", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9888, "facing=east", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9889, "facing=east", "waterlogged=false"));
    }
}
