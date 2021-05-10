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
public final class StrippedAcaciaWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 139, "axis=x"));
        Block.STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 140, "axis=y"));
        Block.STRIPPED_ACACIA_WOOD.addBlockAlternative(new BlockAlternative((short) 141, "axis=z"));
    }
}
