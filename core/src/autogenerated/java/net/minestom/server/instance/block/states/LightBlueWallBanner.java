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
public final class LightBlueWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8169, "facing=north"));
        Block.LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8170, "facing=south"));
        Block.LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8171, "facing=west"));
        Block.LIGHT_BLUE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8172, "facing=east"));
    }
}
