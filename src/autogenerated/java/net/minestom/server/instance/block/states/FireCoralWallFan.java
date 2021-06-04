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
public final class FireCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9628, "facing=north", "waterlogged=true"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9629, "facing=north", "waterlogged=false"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9630, "facing=south", "waterlogged=true"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9631, "facing=south", "waterlogged=false"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9632, "facing=west", "waterlogged=true"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9633, "facing=west", "waterlogged=false"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9634, "facing=east", "waterlogged=true"));
        Block.FIRE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9635, "facing=east", "waterlogged=false"));
    }
}
