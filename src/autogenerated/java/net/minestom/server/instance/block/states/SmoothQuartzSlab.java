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
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11081, "type=top", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11082, "type=top", "waterlogged=false"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11083, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11084, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11085, "type=double", "waterlogged=true"));
        Block.SMOOTH_QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 11086, "type=double", "waterlogged=false"));
    }
}
