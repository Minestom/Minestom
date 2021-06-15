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
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8604, "type=top", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8605, "type=top", "waterlogged=false"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8606, "type=bottom", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8607, "type=bottom", "waterlogged=false"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8608, "type=double", "waterlogged=true"));
        Block.CUT_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8609, "type=double", "waterlogged=false"));
    }
}
