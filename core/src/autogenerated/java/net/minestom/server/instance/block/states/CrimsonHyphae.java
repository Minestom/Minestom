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
public final class CrimsonHyphae {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14989, "axis=x"));
        Block.CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14990, "axis=y"));
        Block.CRIMSON_HYPHAE.addBlockAlternative(new BlockAlternative((short) 14991, "axis=z"));
    }
}
