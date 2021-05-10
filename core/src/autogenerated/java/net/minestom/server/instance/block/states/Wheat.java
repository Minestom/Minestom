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
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3357, "age=0"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3358, "age=1"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3359, "age=2"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3360, "age=3"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3361, "age=4"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3362, "age=5"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3363, "age=6"));
        Block.WHEAT.addBlockAlternative(new BlockAlternative((short) 3364, "age=7"));
    }
}
