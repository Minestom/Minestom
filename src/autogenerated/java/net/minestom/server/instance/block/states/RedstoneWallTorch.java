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
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3958, "facing=north", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3959, "facing=north", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3960, "facing=south", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3961, "facing=south", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3962, "facing=west", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3963, "facing=west", "lit=false"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3964, "facing=east", "lit=true"));
        Block.REDSTONE_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 3965, "facing=east", "lit=false"));
    }
}
