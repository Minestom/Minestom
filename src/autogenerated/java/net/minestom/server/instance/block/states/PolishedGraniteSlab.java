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
public final class PolishedGraniteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11039, "type=top", "waterlogged=true"));
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11040, "type=top", "waterlogged=false"));
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11041, "type=bottom", "waterlogged=true"));
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11042, "type=bottom", "waterlogged=false"));
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11043, "type=double", "waterlogged=true"));
        Block.POLISHED_GRANITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11044, "type=double", "waterlogged=false"));
    }
}
