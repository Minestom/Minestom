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
public final class WaxedExposedCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18508, "type=top", "waterlogged=true"));
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18509, "type=top", "waterlogged=false"));
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18510, "type=bottom", "waterlogged=true"));
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18511, "type=bottom", "waterlogged=false"));
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18512, "type=double", "waterlogged=true"));
        Block.WAXED_EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18513, "type=double", "waterlogged=false"));
    }
}
