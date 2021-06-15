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
public final class OrangeGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9628, "facing=north"));
        Block.ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9629, "facing=south"));
        Block.ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9630, "facing=west"));
        Block.ORANGE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9631, "facing=east"));
    }
}
