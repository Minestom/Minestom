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
public final class LimeGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIME_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9644, "facing=north"));
        Block.LIME_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9645, "facing=south"));
        Block.LIME_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9646, "facing=west"));
        Block.LIME_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9647, "facing=east"));
    }
}
