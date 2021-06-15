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
public final class MagentaWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8411, "facing=north"));
        Block.MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8412, "facing=south"));
        Block.MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8413, "facing=west"));
        Block.MAGENTA_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8414, "facing=east"));
    }
}
