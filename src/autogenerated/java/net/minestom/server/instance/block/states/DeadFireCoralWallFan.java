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
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9588, "facing=north", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9589, "facing=north", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9590, "facing=south", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9591, "facing=south", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9592, "facing=west", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9593, "facing=west", "waterlogged=false"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9594, "facing=east", "waterlogged=true"));
        Block.DEAD_FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9595, "facing=east", "waterlogged=false"));
    }
}
