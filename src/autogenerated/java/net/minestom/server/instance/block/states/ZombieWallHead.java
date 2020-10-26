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
public final class ZombieWallHead {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6550, "facing=north"));
        Block.ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6551, "facing=south"));
        Block.ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6552, "facing=west"));
        Block.ZOMBIE_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6553, "facing=east"));
    }
}
