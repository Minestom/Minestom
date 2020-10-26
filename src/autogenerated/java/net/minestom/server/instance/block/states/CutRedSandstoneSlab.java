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
public final class CutRedSandstoneSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8406, "type=top", "waterlogged=true"));
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8407, "type=top", "waterlogged=false"));
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8408, "type=bottom", "waterlogged=true"));
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8409, "type=bottom", "waterlogged=false"));
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8410, "type=double", "waterlogged=true"));
        Block.CUT_RED_SANDSTONE_SLAB.addBlockAlternative(new BlockAlternative((short) 8411, "type=double", "waterlogged=false"));
    }
}
