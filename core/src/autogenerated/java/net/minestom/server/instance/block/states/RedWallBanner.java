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
public final class RedWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8213, "facing=north"));
        Block.RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8214, "facing=south"));
        Block.RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8215, "facing=west"));
        Block.RED_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8216, "facing=east"));
    }
}
