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
public final class StrippedDarkOakLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 103, "axis=x"));
        Block.STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 104, "axis=y"));
        Block.STRIPPED_DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 105, "axis=z"));
    }
}
