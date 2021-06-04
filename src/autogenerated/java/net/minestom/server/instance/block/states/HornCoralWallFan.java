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
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9636, "facing=north", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9637, "facing=north", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9638, "facing=south", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9639, "facing=south", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9640, "facing=west", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9641, "facing=west", "waterlogged=false"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9642, "facing=east", "waterlogged=true"));
        Block.HORN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9643, "facing=east", "waterlogged=false"));
    }
}
