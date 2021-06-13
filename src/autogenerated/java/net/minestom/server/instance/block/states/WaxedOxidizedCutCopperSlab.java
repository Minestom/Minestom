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
public final class WaxedOxidizedCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18496, "type=top", "waterlogged=true"));
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18497, "type=top", "waterlogged=false"));
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18498, "type=bottom", "waterlogged=true"));
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18499, "type=bottom", "waterlogged=false"));
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18500, "type=double", "waterlogged=true"));
        Block.WAXED_OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18501, "type=double", "waterlogged=false"));
    }
}
