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
public final class YellowGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9640, "facing=north"));
        Block.YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9641, "facing=south"));
        Block.YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9642, "facing=west"));
        Block.YELLOW_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9643, "facing=east"));
    }
}
