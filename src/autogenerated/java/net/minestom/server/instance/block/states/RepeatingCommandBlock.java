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
public final class RepeatingCommandBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9475, "conditional=true", "facing=north"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9476, "conditional=true", "facing=east"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9477, "conditional=true", "facing=south"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9478, "conditional=true", "facing=west"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9479, "conditional=true", "facing=up"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9480, "conditional=true", "facing=down"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9481, "conditional=false", "facing=north"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9482, "conditional=false", "facing=east"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9483, "conditional=false", "facing=south"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9484, "conditional=false", "facing=west"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9485, "conditional=false", "facing=up"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9486, "conditional=false", "facing=down"));
    }
}
