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
public final class WaxedCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18514, "type=top", "waterlogged=true"));
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18515, "type=top", "waterlogged=false"));
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18516, "type=bottom", "waterlogged=true"));
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18517, "type=bottom", "waterlogged=false"));
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18518, "type=double", "waterlogged=true"));
        Block.WAXED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18519, "type=double", "waterlogged=false"));
    }
}
