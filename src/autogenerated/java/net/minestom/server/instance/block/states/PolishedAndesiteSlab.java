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
public final class PolishedAndesiteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11105, "type=top", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11106, "type=top", "waterlogged=false"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11107, "type=bottom", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11108, "type=bottom", "waterlogged=false"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11109, "type=double", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11110, "type=double", "waterlogged=false"));
    }
}
