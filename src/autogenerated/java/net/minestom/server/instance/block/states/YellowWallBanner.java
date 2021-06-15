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
public final class YellowWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8419, "facing=north"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8420, "facing=south"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8421, "facing=west"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8422, "facing=east"));
    }
}
