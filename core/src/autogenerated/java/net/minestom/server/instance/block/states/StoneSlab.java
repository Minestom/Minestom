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
public final class StoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8340, "type=top", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8341, "type=top", "waterlogged=false"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8342, "type=bottom", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8343, "type=bottom", "waterlogged=false"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8344, "type=double", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8345, "type=double", "waterlogged=false"));
    }
}
