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
public final class DarkPrismarineSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7860, "type=top", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7861, "type=top", "waterlogged=false"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7862, "type=bottom", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7863, "type=bottom", "waterlogged=false"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7864, "type=double", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 7865, "type=double", "waterlogged=false"));
    }
}
