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
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8394, "type=top", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8395, "type=top", "waterlogged=false"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8396, "type=bottom", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8397, "type=bottom", "waterlogged=false"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8398, "type=double", "waterlogged=true"));
        Block.QUARTZ_SLAB.addBlockAlternative(new BlockAlternative((short) 8399, "type=double", "waterlogged=false"));
    }
}
