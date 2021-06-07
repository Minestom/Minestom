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
public final class DarkOakWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3842, "facing=north", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3843, "facing=north", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3844, "facing=south", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3845, "facing=south", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3846, "facing=west", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3847, "facing=west", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3848, "facing=east", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3849, "facing=east", "waterlogged=false"));
    }
}
