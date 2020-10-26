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
public final class BirchLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 79, "axis=x"));
        Block.BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 80, "axis=y"));
        Block.BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 81, "axis=z"));
    }
}
