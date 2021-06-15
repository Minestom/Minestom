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
public final class WhiteGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9624, "facing=north"));
        Block.WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9625, "facing=south"));
        Block.WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9626, "facing=west"));
        Block.WHITE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9627, "facing=east"));
    }
}
