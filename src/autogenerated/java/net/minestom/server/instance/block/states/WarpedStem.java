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
public final class WarpedStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 15212, "axis=x"));
        Block.WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 15213, "axis=y"));
        Block.WARPED_STEM.addBlockAlternative(new BlockAlternative((short) 15214, "axis=z"));
    }
}
