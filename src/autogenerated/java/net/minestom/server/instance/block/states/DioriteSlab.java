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
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11111, "type=top", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11112, "type=top", "waterlogged=false"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11113, "type=bottom", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11114, "type=bottom", "waterlogged=false"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11115, "type=double", "waterlogged=true"));
        Block.DIORITE_SLAB.addBlockAlternative(new BlockAlternative((short) 11116, "type=double", "waterlogged=false"));
    }
}
