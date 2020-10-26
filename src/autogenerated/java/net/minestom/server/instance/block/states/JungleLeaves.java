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
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 187, "distance=1", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 188, "distance=1", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 189, "distance=2", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 190, "distance=2", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 191, "distance=3", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 192, "distance=3", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 193, "distance=4", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 194, "distance=4", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 195, "distance=5", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 196, "distance=5", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 197, "distance=6", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 198, "distance=6", "persistent=false"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 199, "distance=7", "persistent=true"));
        Block.JUNGLE_LEAVES.addBlockAlternative(new BlockAlternative((short) 200, "distance=7", "persistent=false"));
    }
}
