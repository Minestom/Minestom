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
public final class Ladder {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3637, "facing=north", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3638, "facing=north", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3639, "facing=south", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3640, "facing=south", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3641, "facing=west", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3642, "facing=west", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3643, "facing=east", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3644, "facing=east", "waterlogged=false"));
    }
}
