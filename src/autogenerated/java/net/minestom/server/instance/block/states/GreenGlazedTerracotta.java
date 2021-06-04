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
public final class GreenGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9430, "facing=north"));
        Block.GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9431, "facing=south"));
        Block.GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9432, "facing=west"));
        Block.GREEN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9433, "facing=east"));
    }
}
