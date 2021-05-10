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
public final class GraniteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10841, "type=top", "waterlogged=true"));
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10842, "type=top", "waterlogged=false"));
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10843, "type=bottom", "waterlogged=true"));
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10844, "type=bottom", "waterlogged=false"));
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10845, "type=double", "waterlogged=true"));
        Block.GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10846, "type=double", "waterlogged=false"));
    }
}
