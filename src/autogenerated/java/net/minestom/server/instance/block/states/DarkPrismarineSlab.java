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
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8106, "type=top", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8107, "type=top", "waterlogged=false"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8108, "type=bottom", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8109, "type=bottom", "waterlogged=false"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8110, "type=double", "waterlogged=true"));
        Block.DARK_PRISMARINE_SLAB.addBlockAlternative(new BlockAlternative((short) 8111, "type=double", "waterlogged=false"));
    }
}
