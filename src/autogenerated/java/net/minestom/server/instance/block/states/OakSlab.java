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
public final class OakSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8304, "type=top", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8305, "type=top", "waterlogged=false"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8306, "type=bottom", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8307, "type=bottom", "waterlogged=false"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8308, "type=double", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8309, "type=double", "waterlogged=false"));
    }
}
