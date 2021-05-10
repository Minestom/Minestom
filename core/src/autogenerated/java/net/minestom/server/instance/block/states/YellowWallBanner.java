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
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8173, "facing=north"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8174, "facing=south"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8175, "facing=west"));
        Block.YELLOW_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8176, "facing=east"));
    }
}
