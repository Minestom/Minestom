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
public final class NetherBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8634, "type=top", "waterlogged=true"));
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8635, "type=top", "waterlogged=false"));
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8636, "type=bottom", "waterlogged=true"));
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8637, "type=bottom", "waterlogged=false"));
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8638, "type=double", "waterlogged=true"));
        Block.NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8639, "type=double", "waterlogged=false"));
    }
}
