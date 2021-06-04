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
public final class SpruceSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8310, "type=top", "waterlogged=true"));
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8311, "type=top", "waterlogged=false"));
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8312, "type=bottom", "waterlogged=true"));
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8313, "type=bottom", "waterlogged=false"));
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8314, "type=double", "waterlogged=true"));
        Block.SPRUCE_SLAB.addBlockAlternative(new BlockAlternative((short) 8315, "type=double", "waterlogged=false"));
    }
}
