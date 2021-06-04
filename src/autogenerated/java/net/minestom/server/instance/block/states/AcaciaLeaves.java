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
public final class AcaciaLeaves {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 201, "distance=1", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 202, "distance=1", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 203, "distance=2", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 204, "distance=2", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 205, "distance=3", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 206, "distance=3", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 207, "distance=4", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 208, "distance=4", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 209, "distance=5", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 210, "distance=5", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 211, "distance=6", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 212, "distance=6", "persistent=false"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 213, "distance=7", "persistent=true"));
        Block.ACACIA_LEAVES.addBlockAlternative(new BlockAlternative((short) 214, "distance=7", "persistent=false"));
    }
}
