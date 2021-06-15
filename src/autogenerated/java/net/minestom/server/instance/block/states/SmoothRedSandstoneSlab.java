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
public final class SmoothRedSandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11045, "type=top", "waterlogged=true"));
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11046, "type=top", "waterlogged=false"));
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11047, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11048, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11049, "type=double", "waterlogged=true"));
        Block.SMOOTH_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11050, "type=double", "waterlogged=false"));
    }
}
