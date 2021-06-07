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
public final class OrangeWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8407, "facing=north"));
        Block.ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8408, "facing=south"));
        Block.ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8409, "facing=west"));
        Block.ORANGE_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8410, "facing=east"));
    }
}
