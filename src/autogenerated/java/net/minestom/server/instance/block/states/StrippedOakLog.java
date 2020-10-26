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
public final class StrippedOakLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 106, "axis=x"));
        Block.STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 107, "axis=y"));
        Block.STRIPPED_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 108, "axis=z"));
    }
}
