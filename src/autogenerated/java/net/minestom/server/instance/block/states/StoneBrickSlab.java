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
public final class StoneBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8628, "type=top", "waterlogged=true"));
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8629, "type=top", "waterlogged=false"));
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8630, "type=bottom", "waterlogged=true"));
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8631, "type=bottom", "waterlogged=false"));
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8632, "type=double", "waterlogged=true"));
        Block.STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8633, "type=double", "waterlogged=false"));
    }
}
