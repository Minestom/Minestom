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
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11051, "type=top", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11052, "type=top", "waterlogged=false"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11053, "type=bottom", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11054, "type=bottom", "waterlogged=false"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11055, "type=double", "waterlogged=true"));
        Block.MOSSY_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 11056, "type=double", "waterlogged=false"));
    }
}
