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
public final class BirchSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8316, "type=top", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8317, "type=top", "waterlogged=false"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8318, "type=bottom", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8319, "type=bottom", "waterlogged=false"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8320, "type=double", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8321, "type=double", "waterlogged=false"));
    }
}
