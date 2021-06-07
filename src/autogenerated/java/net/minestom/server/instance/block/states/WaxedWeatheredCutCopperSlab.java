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
public final class WaxedWeatheredCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18502, "type=top", "waterlogged=true"));
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18503, "type=top", "waterlogged=false"));
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18504, "type=bottom", "waterlogged=true"));
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18505, "type=bottom", "waterlogged=false"));
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18506, "type=double", "waterlogged=true"));
        Block.WAXED_WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18507, "type=double", "waterlogged=false"));
    }
}
