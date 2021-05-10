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
public final class StrippedJungleLog {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 97, "axis=x"));
        Block.STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 98, "axis=y"));
        Block.STRIPPED_JUNGLE_LOG.addBlockAlternative(new BlockAlternative((short) 99, "axis=z"));
    }
}
