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
public final class StrippedAcaciaLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 100, "axis=x"));
        Block.STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 101, "axis=y"));
        Block.STRIPPED_ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 102, "axis=z"));
    }
}
