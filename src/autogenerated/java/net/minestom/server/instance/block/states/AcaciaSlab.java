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
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8574, "type=top", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8575, "type=top", "waterlogged=false"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8576, "type=bottom", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8577, "type=bottom", "waterlogged=false"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8578, "type=double", "waterlogged=true"));
        Block.ACACIA_SLAB.addBlockAlternative(new BlockAlternative((short) 8579, "type=double", "waterlogged=false"));
    }
}
