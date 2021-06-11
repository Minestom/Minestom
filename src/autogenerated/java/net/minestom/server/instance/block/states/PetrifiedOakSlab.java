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
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8610, "type=top", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8611, "type=top", "waterlogged=false"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8612, "type=bottom", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8613, "type=bottom", "waterlogged=false"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8614, "type=double", "waterlogged=true"));
        Block.PETRIFIED_OAK_SLAB.addBlockAlternative(new BlockAlternative((short) 8615, "type=double", "waterlogged=false"));
    }
}
