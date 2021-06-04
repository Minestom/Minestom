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
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5648, "conditional=true", "facing=north"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5649, "conditional=true", "facing=east"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5650, "conditional=true", "facing=south"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5651, "conditional=true", "facing=west"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5652, "conditional=true", "facing=up"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5653, "conditional=true", "facing=down"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5654, "conditional=false", "facing=north"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5655, "conditional=false", "facing=east"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5656, "conditional=false", "facing=south"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5657, "conditional=false", "facing=west"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5658, "conditional=false", "facing=up"));
        Block.COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 5659, "conditional=false", "facing=down"));
    }
}
