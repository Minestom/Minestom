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
public final class PolishedAndesiteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10859, "type=top", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10860, "type=top", "waterlogged=false"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10861, "type=bottom", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10862, "type=bottom", "waterlogged=false"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10863, "type=double", "waterlogged=true"));
        Block.POLISHED_ANDESITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10864, "type=double", "waterlogged=false"));
    }
}
