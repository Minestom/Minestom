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
public final class PurpleWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8443, "facing=north"));
        Block.PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8444, "facing=south"));
        Block.PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8445, "facing=west"));
        Block.PURPLE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8446, "facing=east"));
    }
}
