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
public final class DragonWallHead {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6610, "facing=north"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6611, "facing=south"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6612, "facing=west"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6613, "facing=east"));
    }
}
