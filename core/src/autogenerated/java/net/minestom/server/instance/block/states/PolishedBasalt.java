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
public final class PolishedBasalt {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4005, "axis=x"));
        Block.POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4006, "axis=y"));
        Block.POLISHED_BASALT.addBlockAlternative(new BlockAlternative((short) 4007, "axis=z"));
    }
}
