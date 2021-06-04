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
public final class StrippedWarpedStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14969, "axis=x"));
        Block.STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14970, "axis=y"));
        Block.STRIPPED_WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 14971, "axis=z"));
    }
}
