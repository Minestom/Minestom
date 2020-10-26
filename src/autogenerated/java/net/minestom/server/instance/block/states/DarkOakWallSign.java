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
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3775, "facing=north", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3776, "facing=north", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3777, "facing=south", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3778, "facing=south", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3779, "facing=west", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3780, "facing=west", "waterlogged=false"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3781, "facing=east", "waterlogged=true"));
        Block.DARK_OAK_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3782, "facing=east", "waterlogged=false"));
    }
}
