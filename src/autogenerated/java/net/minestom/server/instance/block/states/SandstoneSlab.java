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
public final class SandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8352, "type=top", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8353, "type=top", "waterlogged=false"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8354, "type=bottom", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8355, "type=bottom", "waterlogged=false"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8356, "type=double", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8357, "type=double", "waterlogged=false"));
    }
}
