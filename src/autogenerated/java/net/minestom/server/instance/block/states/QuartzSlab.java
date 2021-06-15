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
public final class QuartzSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8640, "type=top", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8641, "type=top", "waterlogged=false"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8642, "type=bottom", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8643, "type=bottom", "waterlogged=false"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8644, "type=double", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8645, "type=double", "waterlogged=false"));
    }
}
