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
public final class PrismarineSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8094, "type=top", "waterlogged=true"));
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8095, "type=top", "waterlogged=false"));
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8096, "type=bottom", "waterlogged=true"));
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8097, "type=bottom", "waterlogged=false"));
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8098, "type=double", "waterlogged=true"));
        Block.PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8099, "type=double", "waterlogged=false"));
    }
}
