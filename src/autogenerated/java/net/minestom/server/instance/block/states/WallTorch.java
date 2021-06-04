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
public final class WallTorch {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1436, "facing=north"));
        Block.WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1437, "facing=south"));
        Block.WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1438, "facing=west"));
        Block.WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 1439, "facing=east"));
    }
}
