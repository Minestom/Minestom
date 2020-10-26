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
public final class BrownWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BROWN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8205, "facing=north"));
        Block.BROWN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8206, "facing=south"));
        Block.BROWN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8207, "facing=west"));
        Block.BROWN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8208, "facing=east"));
    }
}
