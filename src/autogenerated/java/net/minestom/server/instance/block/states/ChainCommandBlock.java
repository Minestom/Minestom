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
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9487, "conditional=true", "facing=north"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9488, "conditional=true", "facing=east"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9489, "conditional=true", "facing=south"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9490, "conditional=true", "facing=west"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9491, "conditional=true", "facing=up"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9492, "conditional=true", "facing=down"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9493, "conditional=false", "facing=north"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9494, "conditional=false", "facing=east"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9495, "conditional=false", "facing=south"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9496, "conditional=false", "facing=west"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9497, "conditional=false", "facing=up"));
        Block.CHAIN_COMMAND_BLOCK.addBlockAlternative(new BlockAlternative((short) 9498, "conditional=false", "facing=down"));
    }
}
