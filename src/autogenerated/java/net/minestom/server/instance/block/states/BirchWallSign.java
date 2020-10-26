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
public final class BirchWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3751, "facing=north", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3752, "facing=north", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3753, "facing=south", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3754, "facing=south", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3755, "facing=west", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3756, "facing=west", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3757, "facing=east", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3758, "facing=east", "waterlogged=false"));
    }
}
