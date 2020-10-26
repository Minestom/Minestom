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
public final class HayBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7867, "axis=x"));
        Block.HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7868, "axis=y"));
        Block.HAY_BLOCK.addBlockAlternative(new BlockAlternative((short) 7869, "axis=z"));
    }
}
