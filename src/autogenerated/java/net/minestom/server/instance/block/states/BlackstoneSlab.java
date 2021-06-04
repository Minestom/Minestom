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
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16252, "type=top", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16253, "type=top", "waterlogged=false"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16254, "type=bottom", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16255, "type=bottom", "waterlogged=false"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16256, "type=double", "waterlogged=true"));
        Block.BLACKSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 16257, "type=double", "waterlogged=false"));
    }
}
