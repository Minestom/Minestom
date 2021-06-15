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
public final class DarkOakSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8580, "type=top", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8581, "type=top", "waterlogged=false"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8582, "type=bottom", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8583, "type=bottom", "waterlogged=false"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8584, "type=double", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8585, "type=double", "waterlogged=false"));
    }
}
