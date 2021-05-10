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
public final class PurpurSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8412, "type=top", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8413, "type=top", "waterlogged=false"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8414, "type=bottom", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8415, "type=bottom", "waterlogged=false"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8416, "type=double", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8417, "type=double", "waterlogged=false"));
    }
}
