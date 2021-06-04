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
public final class OakLeaves {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 145, "distance=1", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 146, "distance=1", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 147, "distance=2", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 148, "distance=2", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 149, "distance=3", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 150, "distance=3", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 151, "distance=4", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 152, "distance=4", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 153, "distance=5", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 154, "distance=5", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 155, "distance=6", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 156, "distance=6", "persistent=false"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 157, "distance=7", "persistent=true"));
        Block.OAK_LEAVES.addBlockAlternative(new BlockAlternative((short) 158, "distance=7", "persistent=false"));
    }
}
