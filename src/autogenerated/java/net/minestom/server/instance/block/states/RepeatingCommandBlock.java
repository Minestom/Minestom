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
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9229, "conditional=true", "facing=north"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9230, "conditional=true", "facing=east"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9231, "conditional=true", "facing=south"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9232, "conditional=true", "facing=west"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9233, "conditional=true", "facing=up"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9234, "conditional=true", "facing=down"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9235, "conditional=false", "facing=north"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9236, "conditional=false", "facing=east"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9237, "conditional=false", "facing=south"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9238, "conditional=false", "facing=west"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9239, "conditional=false", "facing=up"));
        Block.REPEATING_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9240, "conditional=false", "facing=down"));
    }
}
