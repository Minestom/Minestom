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
public final class RedstoneWallTorch {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3889, "facing=north", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3890, "facing=north", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3891, "facing=south", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3892, "facing=south", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3893, "facing=west", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3894, "facing=west", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3895, "facing=east", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3896, "facing=east", "lit=false"));
    }
}
