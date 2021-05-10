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
public final class CobblestoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8370, "type=top", "waterlogged=true"));
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8371, "type=top", "waterlogged=false"));
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8372, "type=bottom", "waterlogged=true"));
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8373, "type=bottom", "waterlogged=false"));
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8374, "type=double", "waterlogged=true"));
        Block.COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8375, "type=double", "waterlogged=false"));
    }
}
