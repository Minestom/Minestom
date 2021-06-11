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
public final class ExposedCutCopperSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18156, "type=top", "waterlogged=true"));
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18157, "type=top", "waterlogged=false"));
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18158, "type=bottom", "waterlogged=true"));
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18159, "type=bottom", "waterlogged=false"));
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18160, "type=double", "waterlogged=true"));
        Block.EXPOSED_CUT_COPPER_SLAB.addBlockAlternative(new BlockAlternative((short) 18161, "type=double", "waterlogged=false"));
    }
}
