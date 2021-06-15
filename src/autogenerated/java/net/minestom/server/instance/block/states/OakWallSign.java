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
public final class OakWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3802, "facing=north", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3803, "facing=north", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3804, "facing=south", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3805, "facing=south", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3806, "facing=west", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3807, "facing=west", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3808, "facing=east", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3809, "facing=east", "waterlogged=false"));
    }
}
