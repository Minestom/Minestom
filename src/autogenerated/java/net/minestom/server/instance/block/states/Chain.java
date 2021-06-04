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
public final class Chain {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4729, "axis=x", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4730, "axis=x", "waterlogged=false"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4731, "axis=y", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4732, "axis=y", "waterlogged=false"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4733, "axis=z", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4734, "axis=z", "waterlogged=false"));
    }
}
