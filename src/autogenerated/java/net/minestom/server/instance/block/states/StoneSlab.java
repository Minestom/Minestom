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
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8586, "type=top", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8587, "type=top", "waterlogged=false"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8588, "type=bottom", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8589, "type=bottom", "waterlogged=false"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8590, "type=double", "waterlogged=true"));
        Block.STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8591, "type=double", "waterlogged=false"));
    }
}
