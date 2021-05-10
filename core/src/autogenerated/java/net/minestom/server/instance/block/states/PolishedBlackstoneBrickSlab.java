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
public final class PolishedBlackstoneBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16262, "type=top", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16263, "type=top", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16264, "type=bottom", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16265, "type=bottom", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16266, "type=double", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16267, "type=double", "waterlogged=false"));
    }
}
