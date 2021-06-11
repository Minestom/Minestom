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
public final class BlackstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16498, "type=top", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16499, "type=top", "waterlogged=false"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16500, "type=bottom", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16501, "type=bottom", "waterlogged=false"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16502, "type=double", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16503, "type=double", "waterlogged=false"));
    }
}
