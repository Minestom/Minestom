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
public final class RedNetherBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11099, "type=top", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11100, "type=top", "waterlogged=false"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11101, "type=bottom", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11102, "type=bottom", "waterlogged=false"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11103, "type=double", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11104, "type=double", "waterlogged=false"));
    }
}
