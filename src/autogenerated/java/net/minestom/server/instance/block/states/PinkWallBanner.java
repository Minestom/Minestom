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
public final class PinkWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8181, "facing=north"));
        Block.PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8182, "facing=south"));
        Block.PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8183, "facing=west"));
        Block.PINK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8184, "facing=east"));
    }
}
