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
public final class FrostedIce {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9499, "age=0"));
        Block.FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9500, "age=1"));
        Block.FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9501, "age=2"));
        Block.FROSTED_ICE.addBlockAlternative(new BlockAlternative((short) 9502, "age=3"));
    }
}
