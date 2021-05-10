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
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3921, "layers=1"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3922, "layers=2"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3923, "layers=3"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3924, "layers=4"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3925, "layers=5"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3926, "layers=6"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3927, "layers=7"));
        Block.SNOW.addBlockAlternative(new BlockAlternative((short) 3928, "layers=8"));
    }
}
