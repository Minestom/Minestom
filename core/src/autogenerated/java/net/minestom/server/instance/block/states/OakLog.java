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
public final class OakLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OAK_LOG.addBlockAlternative(new BlockAlternative((short) 73, "axis=x"));
        Block.OAK_LOG.addBlockAlternative(new BlockAlternative((short) 74, "axis=y"));
        Block.OAK_LOG.addBlockAlternative(new BlockAlternative((short) 75, "axis=z"));
    }
}
