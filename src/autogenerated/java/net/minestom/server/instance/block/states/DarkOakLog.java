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
public final class DarkOakLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 91, "axis=x"));
        Block.DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 92, "axis=y"));
        Block.DARK_OAK_LOG.addBlockAlternative(new BlockAlternative((short) 93, "axis=z"));
    }
}
