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
public final class StrippedBirchLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 94, "axis=x"));
        Block.STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 95, "axis=y"));
        Block.STRIPPED_BIRCH_LOG.addBlockAlternative(new BlockAlternative((short) 96, "axis=z"));
    }
}
