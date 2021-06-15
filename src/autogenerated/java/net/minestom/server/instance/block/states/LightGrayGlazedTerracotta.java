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
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9656, "facing=north"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9657, "facing=south"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9658, "facing=west"));
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9659, "facing=east"));
    }
}
