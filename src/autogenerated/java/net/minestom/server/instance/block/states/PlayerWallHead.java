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
public final class PlayerWallHead {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6570, "facing=north"));
        Block.PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6571, "facing=south"));
        Block.PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6572, "facing=west"));
        Block.PLAYER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6573, "facing=east"));
    }
}
