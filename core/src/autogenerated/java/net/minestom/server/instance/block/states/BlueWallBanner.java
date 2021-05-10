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
public final class BlueWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8201, "facing=north"));
        Block.BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8202, "facing=south"));
        Block.BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8203, "facing=west"));
        Block.BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8204, "facing=east"));
    }
}
