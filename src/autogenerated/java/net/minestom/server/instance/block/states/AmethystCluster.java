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
public final class AmethystCluster {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17666, "facing=north", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17667, "facing=north", "waterlogged=false"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17668, "facing=east", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17669, "facing=east", "waterlogged=false"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17670, "facing=south", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17671, "facing=south", "waterlogged=false"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17672, "facing=west", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17673, "facing=west", "waterlogged=false"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17674, "facing=up", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17675, "facing=up", "waterlogged=false"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17676, "facing=down", "waterlogged=true"));
        Block.AMETHYST_CLUSTER.addBlockAlternative(new BlockAlternative((short) 17677, "facing=down", "waterlogged=false"));
    }
}
