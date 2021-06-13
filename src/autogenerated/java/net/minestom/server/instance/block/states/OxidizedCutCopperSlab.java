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
public final class OxidizedCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18144, "type=top", "waterlogged=true"));
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18145, "type=top", "waterlogged=false"));
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18146, "type=bottom", "waterlogged=true"));
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18147, "type=bottom", "waterlogged=false"));
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18148, "type=double", "waterlogged=true"));
        Block.OXIDIZED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18149, "type=double", "waterlogged=false"));
    }
}
