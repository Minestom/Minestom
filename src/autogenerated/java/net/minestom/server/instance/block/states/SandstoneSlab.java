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
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8598, "type=top", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8599, "type=top", "waterlogged=false"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8600, "type=bottom", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8601, "type=bottom", "waterlogged=false"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8602, "type=double", "waterlogged=true"));
        Block.SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8603, "type=double", "waterlogged=false"));
    }
}
