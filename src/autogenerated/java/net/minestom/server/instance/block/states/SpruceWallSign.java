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
public final class SpruceWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3810, "facing=north", "waterlogged=true"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3811, "facing=north", "waterlogged=false"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3812, "facing=south", "waterlogged=true"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3813, "facing=south", "waterlogged=false"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3814, "facing=west", "waterlogged=true"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3815, "facing=west", "waterlogged=false"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3816, "facing=east", "waterlogged=true"));
        Block.SPRUCE_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3817, "facing=east", "waterlogged=false"));
    }
}
