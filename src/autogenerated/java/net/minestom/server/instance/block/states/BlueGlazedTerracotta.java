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
public final class BlueGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9668, "facing=north"));
        Block.BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9669, "facing=south"));
        Block.BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9670, "facing=west"));
        Block.BLUE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9671, "facing=east"));
    }
}
