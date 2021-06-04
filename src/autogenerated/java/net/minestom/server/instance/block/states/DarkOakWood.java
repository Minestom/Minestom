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
public final class DarkOakWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 124, "axis=x"));
        Block.DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 125, "axis=y"));
        Block.DARK_OAK_WOOD.addBlockAlternative(new BlockAlternative((short) 126, "axis=z"));
    }
}
