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
public final class BubbleCoralWallFan {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9620, "facing=north", "waterlogged=true"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9621, "facing=north", "waterlogged=false"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9622, "facing=south", "waterlogged=true"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9623, "facing=south", "waterlogged=false"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9624, "facing=west", "waterlogged=true"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9625, "facing=west", "waterlogged=false"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9626, "facing=east", "waterlogged=true"));
        Block.BUBBLE_CORAL_WALL_FAN.addBlockAlternative(new BlockAlternative((short) 9627, "facing=east", "waterlogged=false"));
    }
}
