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
public final class PetrifiedOakSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8364, "type=top", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8365, "type=top", "waterlogged=false"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8366, "type=bottom", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8367, "type=bottom", "waterlogged=false"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8368, "type=double", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8369, "type=double", "waterlogged=false"));
    }
}
