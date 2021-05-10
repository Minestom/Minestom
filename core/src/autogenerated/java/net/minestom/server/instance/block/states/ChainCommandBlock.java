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
public final class ChainCommandBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9241, "conditional=true", "facing=north"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9242, "conditional=true", "facing=east"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9243, "conditional=true", "facing=south"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9244, "conditional=true", "facing=west"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9245, "conditional=true", "facing=up"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9246, "conditional=true", "facing=down"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9247, "conditional=false", "facing=north"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9248, "conditional=false", "facing=east"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9249, "conditional=false", "facing=south"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9250, "conditional=false", "facing=west"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9251, "conditional=false", "facing=up"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9252, "conditional=false", "facing=down"));
    }
}
