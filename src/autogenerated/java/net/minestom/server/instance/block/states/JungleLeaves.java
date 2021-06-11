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
public final class JungleLeaves {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 190, "distance=1", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 191, "distance=1", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 192, "distance=2", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 193, "distance=2", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 194, "distance=3", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 195, "distance=3", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 196, "distance=4", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 197, "distance=4", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 198, "distance=5", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 199, "distance=5", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 200, "distance=6", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 201, "distance=6", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 202, "distance=7", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 203, "distance=7", "persistent=false"));
    }
}
