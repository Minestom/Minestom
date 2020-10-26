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
public final class EndStoneBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10823, "type=top", "waterlogged=true"));
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10824, "type=top", "waterlogged=false"));
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10825, "type=bottom", "waterlogged=true"));
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10826, "type=bottom", "waterlogged=false"));
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10827, "type=double", "waterlogged=true"));
        Block.END_STONE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 10828, "type=double", "waterlogged=false"));
    }
}
