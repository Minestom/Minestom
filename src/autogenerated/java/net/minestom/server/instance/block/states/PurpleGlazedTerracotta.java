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
public final class PurpleGlazedTerracotta {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9664, "facing=north"));
        Block.PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9665, "facing=south"));
        Block.PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9666, "facing=west"));
        Block.PURPLE_GLAZED_TERRACOTTA.addBlockAlternative(new BlockAlternative((short) 9667, "facing=east"));
    }
}
