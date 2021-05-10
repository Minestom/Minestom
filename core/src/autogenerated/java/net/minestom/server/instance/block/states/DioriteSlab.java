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
public final class DioriteSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10865, "type=top", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10866, "type=top", "waterlogged=false"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10867, "type=bottom", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10868, "type=bottom", "waterlogged=false"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10869, "type=double", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 10870, "type=double", "waterlogged=false"));
    }
}
