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
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15727, "facing=north", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15728, "facing=north", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15729, "facing=south", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15730, "facing=south", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15731, "facing=west", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15732, "facing=west", "waterlogged=false"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15733, "facing=east", "waterlogged=true"));
        Block.CRIMSON_WALL_SIGN.addBlockAlternative(new BlockAlternative((short) 15734, "facing=east", "waterlogged=false"));
    }
}
