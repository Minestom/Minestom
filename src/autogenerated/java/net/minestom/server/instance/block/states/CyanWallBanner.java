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
public final class CyanWallBanner {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8439, "facing=north"));
        Block.CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8440, "facing=south"));
        Block.CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8441, "facing=west"));
        Block.CYAN_WALL_BANNER.addBlockAlternative(new BlockAlternative((short) 8442, "facing=east"));
    }
}
