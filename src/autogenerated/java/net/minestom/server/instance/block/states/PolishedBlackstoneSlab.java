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
public final class PolishedBlackstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16999, "type=top", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 17000, "type=top", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 17001, "type=bottom", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 17002, "type=bottom", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 17003, "type=double", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 17004, "type=double", "waterlogged=false"));
    }
}
