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
public final class BrownGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9672, "facing=north"));
        Block.BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9673, "facing=south"));
        Block.BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9674, "facing=west"));
        Block.BROWN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9675, "facing=east"));
    }
}
