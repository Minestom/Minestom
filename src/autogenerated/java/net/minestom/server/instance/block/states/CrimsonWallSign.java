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
public final class CrimsonWallSign {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15973, "facing=north", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15974, "facing=north", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15975, "facing=south", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15976, "facing=south", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15977, "facing=west", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15978, "facing=west", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15979, "facing=east", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15980, "facing=east", "waterlogged=false"));
    }
}
