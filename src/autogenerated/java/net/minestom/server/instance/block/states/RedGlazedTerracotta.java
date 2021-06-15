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
public final class RedGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9680, "facing=north"));
        Block.RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9681, "facing=south"));
        Block.RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9682, "facing=west"));
        Block.RED_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9683, "facing=east"));
    }
}
