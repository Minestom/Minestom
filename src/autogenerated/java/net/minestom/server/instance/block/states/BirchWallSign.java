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
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3818, "facing=north", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3819, "facing=north", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3820, "facing=south", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3821, "facing=south", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3822, "facing=west", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3823, "facing=west", "waterlogged=false"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3824, "facing=east", "waterlogged=true"));
        Block.BIRCH_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3825, "facing=east", "waterlogged=false"));
    }
}
