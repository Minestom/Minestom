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
public final class GreenWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8209, "facing=north"));
        Block.GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8210, "facing=south"));
        Block.GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8211, "facing=west"));
        Block.GREEN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8212, "facing=east"));
    }
}
