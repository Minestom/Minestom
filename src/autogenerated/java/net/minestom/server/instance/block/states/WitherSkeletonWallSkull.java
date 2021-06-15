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
public final class WitherSkeletonWallSkull {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WITHER_SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6732, "facing=north"));
        Block.WITHER_SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6733, "facing=south"));
        Block.WITHER_SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6734, "facing=west"));
        Block.WITHER_SKELETON_WALL_SKULL.addBlockAlternative(new BlockAlternative((short) 6735, "facing=east"));
    }
}
