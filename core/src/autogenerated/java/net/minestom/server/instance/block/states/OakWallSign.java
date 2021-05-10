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
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3735, "facing=north", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3736, "facing=north", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3737, "facing=south", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3738, "facing=south", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3739, "facing=west", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3740, "facing=west", "waterlogged=false"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3741, "facing=east", "waterlogged=true"));
        Block.OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3742, "facing=east", "waterlogged=false"));
    }
}
