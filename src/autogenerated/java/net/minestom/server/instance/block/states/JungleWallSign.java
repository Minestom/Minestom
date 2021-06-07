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
public final class JungleWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3834, "facing=north", "waterlogged=true"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3835, "facing=north", "waterlogged=false"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3836, "facing=south", "waterlogged=true"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3837, "facing=south", "waterlogged=false"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3838, "facing=west", "waterlogged=true"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3839, "facing=west", "waterlogged=false"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3840, "facing=east", "waterlogged=true"));
        Block.JUNGLE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3841, "facing=east", "waterlogged=false"));
    }
}
