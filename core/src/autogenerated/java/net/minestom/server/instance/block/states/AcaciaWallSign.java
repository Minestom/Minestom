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
public final class AcaciaWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3759, "facing=north", "waterlogged=true"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3760, "facing=north", "waterlogged=false"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3761, "facing=south", "waterlogged=true"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3762, "facing=south", "waterlogged=false"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3763, "facing=west", "waterlogged=true"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3764, "facing=west", "waterlogged=false"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3765, "facing=east", "waterlogged=true"));
        Block.ACACIA_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 3766, "facing=east", "waterlogged=false"));
    }
}
