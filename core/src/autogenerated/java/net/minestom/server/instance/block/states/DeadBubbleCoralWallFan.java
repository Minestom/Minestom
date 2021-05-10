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
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9580, "facing=north", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9581, "facing=north", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9582, "facing=south", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9583, "facing=south", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9584, "facing=west", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9585, "facing=west", "waterlogged=false"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9586, "facing=east", "waterlogged=true"));
        Block.DEAD_BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9587, "facing=east", "waterlogged=false"));
    }
}
