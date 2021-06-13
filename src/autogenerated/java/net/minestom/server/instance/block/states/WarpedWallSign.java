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
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15981, "facing=north", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15982, "facing=north", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15983, "facing=south", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15984, "facing=south", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15985, "facing=west", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15986, "facing=west", "waterlogged=false"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15987, "facing=east", "waterlogged=true"));
        Block.WARPED_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15988, "facing=east", "waterlogged=false"));
    }
}
