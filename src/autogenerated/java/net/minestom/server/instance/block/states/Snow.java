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
public final class Snow {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3990, "layers=1"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3991, "layers=2"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3992, "layers=3"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3993, "layers=4"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3994, "layers=5"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3995, "layers=6"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3996, "layers=7"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3997, "layers=8"));
    }
}
