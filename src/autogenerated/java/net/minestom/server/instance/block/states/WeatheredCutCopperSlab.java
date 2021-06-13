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
public final class WeatheredCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18150, "type=top", "waterlogged=true"));
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18151, "type=top", "waterlogged=false"));
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18152, "type=bottom", "waterlogged=true"));
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18153, "type=bottom", "waterlogged=false"));
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18154, "type=double", "waterlogged=true"));
        Block.WEATHERED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18155, "type=double", "waterlogged=false"));
    }
}
