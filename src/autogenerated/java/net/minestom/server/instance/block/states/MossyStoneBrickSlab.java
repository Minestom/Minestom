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
public final class MossyStoneBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10805, "type=top", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10806, "type=top", "waterlogged=false"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10807, "type=bottom", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10808, "type=bottom", "waterlogged=false"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10809, "type=double", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10810, "type=double", "waterlogged=false"));
    }
}
