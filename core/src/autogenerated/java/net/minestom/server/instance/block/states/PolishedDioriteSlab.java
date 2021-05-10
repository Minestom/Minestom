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
public final class PolishedDioriteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10811, "type=top", "waterlogged=true"));
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10812, "type=top", "waterlogged=false"));
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10813, "type=bottom", "waterlogged=true"));
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10814, "type=bottom", "waterlogged=false"));
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10815, "type=double", "waterlogged=true"));
        Block.POLISHED_DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10816, "type=double", "waterlogged=false"));
    }
}
