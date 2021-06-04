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
public final class SkeletonWallSkull {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6510, "facing=north"));
        Block.SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6511, "facing=south"));
        Block.SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6512, "facing=west"));
        Block.SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6513, "facing=east"));
    }
}
