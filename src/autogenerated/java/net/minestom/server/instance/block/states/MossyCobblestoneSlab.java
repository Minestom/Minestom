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
public final class MossyCobblestoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11063, "type=top", "waterlogged=true"));
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11064, "type=top", "waterlogged=false"));
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11065, "type=bottom", "waterlogged=true"));
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11066, "type=bottom", "waterlogged=false"));
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11067, "type=double", "waterlogged=true"));
        Block.MOSSY_COBBLESTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 11068, "type=double", "waterlogged=false"));
    }
}
