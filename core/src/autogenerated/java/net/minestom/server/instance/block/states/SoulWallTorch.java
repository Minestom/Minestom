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
public final class SoulWallTorch {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4009, "facing=north"));
        Block.SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4010, "facing=south"));
        Block.SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4011, "facing=west"));
        Block.SOUL_WALL_TORCH.addBlockAlternative(new BlockAlternative((short) 4012, "facing=east"));
    }
}
