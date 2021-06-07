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
public final class SmoothSandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11075, "type=top", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11076, "type=top", "waterlogged=false"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11077, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11078, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11079, "type=double", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11080, "type=double", "waterlogged=false"));
    }
}
