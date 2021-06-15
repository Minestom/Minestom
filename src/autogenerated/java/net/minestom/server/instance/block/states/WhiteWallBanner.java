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
public final class WhiteWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8403, "facing=north"));
        Block.WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8404, "facing=south"));
        Block.WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8405, "facing=west"));
        Block.WHITE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8406, "facing=east"));
    }
}
