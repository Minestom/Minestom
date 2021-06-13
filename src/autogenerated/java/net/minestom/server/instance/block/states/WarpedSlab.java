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
public final class WarpedSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15307, "type=top", "waterlogged=true"));
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15308, "type=top", "waterlogged=false"));
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15309, "type=bottom", "waterlogged=true"));
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15310, "type=bottom", "waterlogged=false"));
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15311, "type=double", "waterlogged=true"));
        Block.WARPED_SLAB.addBlockAlternative(new BlockAlternative((short) 15312, "type=double", "waterlogged=false"));
    }
}
