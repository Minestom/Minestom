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
public final class SpruceWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 112, "axis=x"));
        Block.SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 113, "axis=y"));
        Block.SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 114, "axis=z"));
    }
}
