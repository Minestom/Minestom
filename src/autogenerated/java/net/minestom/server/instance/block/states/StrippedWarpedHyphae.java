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
public final class StrippedWarpedHyphae {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 15221, "axis=x"));
        Block.STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 15222, "axis=y"));
        Block.STRIPPED_WARPED_HYPHAE.addBlockAlternative(new BlockAlternative((short) 15223, "axis=z"));
    }
}
