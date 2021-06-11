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
public final class Wheat {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3414, "age=0"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3415, "age=1"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3416, "age=2"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3417, "age=3"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3418, "age=4"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3419, "age=5"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3420, "age=6"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3421, "age=7"));
    }
}
