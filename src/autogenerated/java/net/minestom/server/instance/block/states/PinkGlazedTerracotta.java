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
public final class PinkGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9648, "facing=north"));
        Block.PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9649, "facing=south"));
        Block.PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9650, "facing=west"));
        Block.PINK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9651, "facing=east"));
    }
}
