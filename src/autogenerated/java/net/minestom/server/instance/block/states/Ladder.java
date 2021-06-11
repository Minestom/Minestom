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
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3694, "facing=north", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3695, "facing=north", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3696, "facing=south", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3697, "facing=south", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3698, "facing=west", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3699, "facing=west", "waterlogged=false"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3700, "facing=east", "waterlogged=true"));
        Block.LADDER.addBlockAlternative(new BlockAlternative((short) 3701, "facing=east", "waterlogged=false"));
    }
}
