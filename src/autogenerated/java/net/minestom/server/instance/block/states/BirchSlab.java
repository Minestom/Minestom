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
public final class BirchSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8562, "type=top", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8563, "type=top", "waterlogged=false"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8564, "type=bottom", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8565, "type=bottom", "waterlogged=false"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8566, "type=double", "waterlogged=true"));
        Block.BIRCH_SLAB.addBlockAlternative(new BlockAlternative((short) 8567, "type=double", "waterlogged=false"));
    }
}
