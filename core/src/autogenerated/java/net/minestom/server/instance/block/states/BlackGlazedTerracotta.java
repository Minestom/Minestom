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
public final class BlackGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9438, "facing=north"));
        Block.BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9439, "facing=south"));
        Block.BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9440, "facing=west"));
        Block.BLACK_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9441, "facing=east"));
    }
}
