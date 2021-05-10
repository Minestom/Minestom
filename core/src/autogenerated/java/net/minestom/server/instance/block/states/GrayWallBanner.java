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
public final class GrayWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8185, "facing=north"));
        Block.GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8186, "facing=south"));
        Block.GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8187, "facing=west"));
        Block.GRAY_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8188, "facing=east"));
    }
}
