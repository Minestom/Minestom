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
public final class BrainCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9612, "facing=north", "waterlogged=true"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9613, "facing=north", "waterlogged=false"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9614, "facing=south", "waterlogged=true"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9615, "facing=south", "waterlogged=false"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9616, "facing=west", "waterlogged=true"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9617, "facing=west", "waterlogged=false"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9618, "facing=east", "waterlogged=true"));
        Block.BRAIN_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9619, "facing=east", "waterlogged=false"));
    }
}
