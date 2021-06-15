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
public final class CrimsonStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 15229, "axis=x"));
        Block.CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 15230, "axis=y"));
        Block.CRIMSON_STEM.addBlockAlternative(new BlockAlternative((short) 15231, "axis=z"));
    }
}
