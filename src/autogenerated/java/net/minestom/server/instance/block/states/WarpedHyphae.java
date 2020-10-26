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
public final class WarpedHyphae {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14972, "axis=x"));
        Block.WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14973, "axis=y"));
        Block.WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14974, "axis=z"));
    }
}
