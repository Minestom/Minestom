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
public final class WarpedWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15735, "facing=north", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15736, "facing=north", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15737, "facing=south", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15738, "facing=south", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15739, "facing=west", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15740, "facing=west", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15741, "facing=east", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15742, "facing=east", "waterlogged=false"));
    }
}
