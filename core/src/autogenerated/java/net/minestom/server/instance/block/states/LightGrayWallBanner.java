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
public final class LightGrayWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8189, "facing=north"));
        Block.LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8190, "facing=south"));
        Block.LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8191, "facing=west"));
        Block.LIGHT_GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8192, "facing=east"));
    }
}
