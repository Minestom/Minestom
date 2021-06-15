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
public final class Basalt {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BASALT.addBlockAlternative(new BlockAlternative((short) 4071, "axis=x"));
        Block.BASALT.addBlockAlternative(new BlockAlternative((short) 4072, "axis=y"));
        Block.BASALT.addBlockAlternative(new BlockAlternative((short) 4073, "axis=z"));
    }
}
