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
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16508, "type=top", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16509, "type=top", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16510, "type=bottom", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16511, "type=bottom", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16512, "type=double", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 16513, "type=double", "waterlogged=false"));
    }
}
