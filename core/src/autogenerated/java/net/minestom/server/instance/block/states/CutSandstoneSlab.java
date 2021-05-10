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
public final class CutSandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8358, "type=top", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8359, "type=top", "waterlogged=false"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8360, "type=bottom", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8361, "type=bottom", "waterlogged=false"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8362, "type=double", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8363, "type=double", "waterlogged=false"));
    }
}
