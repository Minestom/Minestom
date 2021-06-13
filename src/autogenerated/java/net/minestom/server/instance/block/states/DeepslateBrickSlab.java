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
public final class DeepslateBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20000, "type=top", "waterlogged=true"));
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20001, "type=top", "waterlogged=false"));
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20002, "type=bottom", "waterlogged=true"));
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20003, "type=bottom", "waterlogged=false"));
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20004, "type=double", "waterlogged=true"));
        Block.DEEPSLATE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 20005, "type=double", "waterlogged=false"));
    }
}
