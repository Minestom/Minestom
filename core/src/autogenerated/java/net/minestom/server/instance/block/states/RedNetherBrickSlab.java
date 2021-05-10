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
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10853, "type=top", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10854, "type=top", "waterlogged=false"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10855, "type=bottom", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10856, "type=bottom", "waterlogged=false"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10857, "type=double", "waterlogged=true"));
        Block.RED_NETHER_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10858, "type=double", "waterlogged=false"));
    }
}
