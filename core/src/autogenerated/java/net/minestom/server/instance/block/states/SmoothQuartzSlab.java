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
public final class SmoothQuartzSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10835, "type=top", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10836, "type=top", "waterlogged=false"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10837, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10838, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10839, "type=double", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 10840, "type=double", "waterlogged=false"));
    }
}
