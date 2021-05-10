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
public final class LightGrayGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9410, "facing=north"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9411, "facing=south"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9412, "facing=west"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9413, "facing=east"));
    }
}
