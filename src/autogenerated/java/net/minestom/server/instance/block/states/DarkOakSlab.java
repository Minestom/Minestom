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
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8334, "type=top", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8335, "type=top", "waterlogged=false"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8336, "type=bottom", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8337, "type=bottom", "waterlogged=false"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8338, "type=double", "waterlogged=true"));
        Block.DARK_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8339, "type=double", "waterlogged=false"));
    }
}
