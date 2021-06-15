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
public final class NetherWart {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5329, "age=0"));
        Block.NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5330, "age=1"));
        Block.NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5331, "age=2"));
        Block.NETHER_WART.addBlockAlternative(new BlockAlternative((short) 5332, "age=3"));
    }
}
