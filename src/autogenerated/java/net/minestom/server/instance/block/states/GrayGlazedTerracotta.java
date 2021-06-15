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
public final class GrayGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9652, "facing=north"));
        Block.GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9653, "facing=south"));
        Block.GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9654, "facing=west"));
        Block.GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9655, "facing=east"));
    }
}
