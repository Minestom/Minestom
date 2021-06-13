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
public final class CommandBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5850, "conditional=true", "facing=north"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5851, "conditional=true", "facing=east"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5852, "conditional=true", "facing=south"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5853, "conditional=true", "facing=west"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5854, "conditional=true", "facing=up"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5855, "conditional=true", "facing=down"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5856, "conditional=false", "facing=north"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5857, "conditional=false", "facing=east"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5858, "conditional=false", "facing=south"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5859, "conditional=false", "facing=west"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5860, "conditional=false", "facing=up"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5861, "conditional=false", "facing=down"));
    }
}
