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
public final class PolishedDeepslateSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19178, "type=top", "waterlogged=true"));
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19179, "type=top", "waterlogged=false"));
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19180, "type=bottom", "waterlogged=true"));
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19181, "type=bottom", "waterlogged=false"));
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19182, "type=double", "waterlogged=true"));
        Block.POLISHED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 19183, "type=double", "waterlogged=false"));
    }
}
