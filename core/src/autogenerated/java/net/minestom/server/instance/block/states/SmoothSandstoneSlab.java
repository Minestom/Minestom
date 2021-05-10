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
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10829, "type=top", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10830, "type=top", "waterlogged=false"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10831, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10832, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10833, "type=double", "waterlogged=true"));
        Block.SMOOTH_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 10834, "type=double", "waterlogged=false"));
    }
}
