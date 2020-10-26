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
public final class AcaciaSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8328, "type=top", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8329, "type=top", "waterlogged=false"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8330, "type=bottom", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8331, "type=bottom", "waterlogged=false"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8332, "type=double", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8333, "type=double", "waterlogged=false"));
    }
}
