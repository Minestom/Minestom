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
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16753, "type=top", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16754, "type=top", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16755, "type=bottom", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16756, "type=bottom", "waterlogged=false"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16757, "type=double", "waterlogged=true"));
        Block.POLISHED_BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16758, "type=double", "waterlogged=false"));
    }
}
