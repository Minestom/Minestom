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
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6812, "facing=north"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6813, "facing=south"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6814, "facing=west"));
        Block.DRAGON_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6815, "facing=east"));
    }
}
