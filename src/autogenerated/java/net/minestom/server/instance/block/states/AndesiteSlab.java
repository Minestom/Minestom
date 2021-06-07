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
public final class AndesiteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11093, "type=top", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11094, "type=top", "waterlogged=false"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11095, "type=bottom", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11096, "type=bottom", "waterlogged=false"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11097, "type=double", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11098, "type=double", "waterlogged=false"));
    }
}
