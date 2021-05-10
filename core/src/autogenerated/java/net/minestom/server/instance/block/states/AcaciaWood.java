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
public final class AcaciaWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 121, "axis=x"));
        Block.ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 122, "axis=y"));
        Block.ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 123, "axis=z"));
    }
}
