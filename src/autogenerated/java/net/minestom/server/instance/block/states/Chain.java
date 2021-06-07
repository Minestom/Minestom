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
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4798, "axis=x", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4799, "axis=x", "waterlogged=false"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4800, "axis=y", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4801, "axis=y", "waterlogged=false"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4802, "axis=z", "waterlogged=true"));
        Block.CHAIN.addBlockAlternative(new BlockAlternative((short) 4803, "axis=z", "waterlogged=false"));
    }
}
