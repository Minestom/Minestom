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
public final class SmoothStoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8592, "type=top", "waterlogged=true"));
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8593, "type=top", "waterlogged=false"));
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8594, "type=bottom", "waterlogged=true"));
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8595, "type=bottom", "waterlogged=false"));
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8596, "type=double", "waterlogged=true"));
        Block.SMOOTH_STONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8597, "type=double", "waterlogged=false"));
    }
}
