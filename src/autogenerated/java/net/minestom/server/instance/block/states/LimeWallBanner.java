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
public final class LimeWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8423, "facing=north"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8424, "facing=south"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8425, "facing=west"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8426, "facing=east"));
    }
}
