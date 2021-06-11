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
public final class CrimsonSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15301, "type=top", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15302, "type=top", "waterlogged=false"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15303, "type=bottom", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15304, "type=bottom", "waterlogged=false"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15305, "type=double", "waterlogged=true"));
        Block.CRIMSON_SLAB.addBlockAlternative(new BlockAlternative((short) 15306, "type=double", "waterlogged=false"));
    }
}
