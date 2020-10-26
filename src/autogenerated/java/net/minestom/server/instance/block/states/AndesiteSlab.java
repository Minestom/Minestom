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
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10847, "type=top", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10848, "type=top", "waterlogged=false"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10849, "type=bottom", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10850, "type=bottom", "waterlogged=false"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10851, "type=double", "waterlogged=true"));
        Block.ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10852, "type=double", "waterlogged=false"));
    }
}
