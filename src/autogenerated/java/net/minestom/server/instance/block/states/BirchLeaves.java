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
public final class BirchLeaves {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 173, "distance=1", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 174, "distance=1", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 175, "distance=2", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 176, "distance=2", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 177, "distance=3", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 178, "distance=3", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 179, "distance=4", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 180, "distance=4", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 181, "distance=5", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 182, "distance=5", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 183, "distance=6", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 184, "distance=6", "persistent=false"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 185, "distance=7", "persistent=true"));
        Block.BIRCH_LEAVES.addBlockAlternative(new BlockAlternative((short) 186, "distance=7", "persistent=false"));
    }
}
