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
public final class CutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18162, "type=top", "waterlogged=true"));
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18163, "type=top", "waterlogged=false"));
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18164, "type=bottom", "waterlogged=true"));
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18165, "type=bottom", "waterlogged=false"));
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18166, "type=double", "waterlogged=true"));
        Block.CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18167, "type=double", "waterlogged=false"));
    }
}
