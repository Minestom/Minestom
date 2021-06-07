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
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8658, "type=top", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8659, "type=top", "waterlogged=false"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8660, "type=bottom", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8661, "type=bottom", "waterlogged=false"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8662, "type=double", "waterlogged=true"));
        Block.PURPUR_SLAB.addBlockAlternative(new BlockAlternative((short) 8663, "type=double", "waterlogged=false"));
    }
}
