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
public final class AcaciaLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 85, "axis=x"));
        Block.ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 86, "axis=y"));
        Block.ACACIA_LOG.addBlockAlternative(new BlockAlternative((short) 87, "axis=z"));
    }
}
