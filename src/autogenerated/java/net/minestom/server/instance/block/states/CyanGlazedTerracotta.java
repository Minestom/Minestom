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
public final class CyanGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9414, "facing=north"));
        Block.CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9415, "facing=south"));
        Block.CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9416, "facing=west"));
        Block.CYAN_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9417, "facing=east"));
    }
}
