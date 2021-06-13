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
public final class Deepslate {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 18683, "axis=x"));
        Block.DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 18684, "axis=y"));
        Block.DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 18685, "axis=z"));
    }
}
