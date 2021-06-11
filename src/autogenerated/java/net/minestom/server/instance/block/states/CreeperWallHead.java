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
public final class CreeperWallHead {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6792, "facing=north"));
        Block.CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6793, "facing=south"));
        Block.CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6794, "facing=west"));
        Block.CREEPER_WALL_HEAD.addBlockAlternative(new BlockAlternative((short) 6795, "facing=east"));
    }
}
