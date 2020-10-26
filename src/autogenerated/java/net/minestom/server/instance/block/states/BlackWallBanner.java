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
public final class BlackWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8217, "facing=north"));
        Block.BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8218, "facing=south"));
        Block.BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8219, "facing=west"));
        Block.BLACK_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8220, "facing=east"));
    }
}
