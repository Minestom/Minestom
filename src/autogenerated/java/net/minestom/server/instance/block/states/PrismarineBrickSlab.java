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
public final class PrismarineBrickSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8100, "type=top", "waterlogged=true"));
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8101, "type=top", "waterlogged=false"));
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8102, "type=bottom", "waterlogged=true"));
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8103, "type=bottom", "waterlogged=false"));
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8104, "type=double", "waterlogged=true"));
        Block.PRISMARINE_BRICK_SLAB.addBlockAlternative(new BlockAlternative((short) 8105, "type=double", "waterlogged=false"));
    }
}
