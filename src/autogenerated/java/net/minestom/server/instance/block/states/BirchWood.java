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
public final class BirchWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 118, "axis=x"));
        Block.BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 119, "axis=y"));
        Block.BIRCH_WOOD.addBlockAlternative(new BlockAlternative((short) 120, "axis=z"));
    }
}
