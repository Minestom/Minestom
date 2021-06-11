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
public final class DeadFireCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9834, "facing=north", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9835, "facing=north", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9836, "facing=south", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9837, "facing=south", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9838, "facing=west", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9839, "facing=west", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9840, "facing=east", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9841, "facing=east", "waterlogged=false"));
    }
}
