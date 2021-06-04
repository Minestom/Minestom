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
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8177, "facing=north"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8178, "facing=south"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8179, "facing=west"));
        Block.LIME_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8180, "facing=east"));
    }
}
