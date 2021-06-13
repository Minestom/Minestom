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
public final class DeepslateRedstoneOre {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEEPSLATE_REDSTONE_ORE.addBlockAlternative(new BlockAlternative((short) 3954, "lit=true"));
        Block.DEEPSLATE_REDSTONE_ORE.addBlockAlternative(new BlockAlternative((short) 3955, "lit=false"));
    }
}
