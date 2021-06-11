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
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8550, "type=top", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8551, "type=top", "waterlogged=false"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8552, "type=bottom", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8553, "type=bottom", "waterlogged=false"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8554, "type=double", "waterlogged=true"));
        Block.OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8555, "type=double", "waterlogged=false"));
    }
}
