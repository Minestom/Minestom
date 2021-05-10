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
public final class BrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8376, "type=top", "waterlogged=true"));
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8377, "type=top", "waterlogged=false"));
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8378, "type=bottom", "waterlogged=true"));
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8379, "type=bottom", "waterlogged=false"));
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8380, "type=double", "waterlogged=true"));
        Block.BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8381, "type=double", "waterlogged=false"));
    }
}
