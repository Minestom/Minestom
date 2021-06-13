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
public final class DeadBubbleCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9826, "facing=north", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9827, "facing=north", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9828, "facing=south", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9829, "facing=south", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9830, "facing=west", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9831, "facing=west", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9832, "facing=east", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9833, "facing=east", "waterlogged=false"));
    }
}
