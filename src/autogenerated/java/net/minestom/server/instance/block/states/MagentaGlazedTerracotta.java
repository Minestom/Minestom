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
public final class MagentaGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9632, "facing=north"));
        Block.MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9633, "facing=south"));
        Block.MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9634, "facing=west"));
        Block.MAGENTA_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9635, "facing=east"));
    }
}
