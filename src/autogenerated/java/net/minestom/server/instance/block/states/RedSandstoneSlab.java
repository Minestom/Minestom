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
public final class RedSandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8400, "type=top", "waterlogged=true"));
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8401, "type=top", "waterlogged=false"));
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8402, "type=bottom", "waterlogged=true"));
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8403, "type=bottom", "waterlogged=false"));
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8404, "type=double", "waterlogged=true"));
        Block.RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8405, "type=double", "waterlogged=false"));
    }
}
