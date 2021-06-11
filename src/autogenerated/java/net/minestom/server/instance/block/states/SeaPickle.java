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
public final class SeaPickle {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9890, "pickles=1", "waterlogged=true"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9891, "pickles=1", "waterlogged=false"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9892, "pickles=2", "waterlogged=true"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9893, "pickles=2", "waterlogged=false"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9894, "pickles=3", "waterlogged=true"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9895, "pickles=3", "waterlogged=false"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9896, "pickles=4", "waterlogged=true"));
        Block.SEA_PICKLE.addBlockAlternative(new BlockAlternative((short) 9897, "pickles=4", "waterlogged=false"));
    }
}
