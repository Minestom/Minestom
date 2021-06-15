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
public final class StrippedSpruceWood {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 133, "axis=x"));
        Block.STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 134, "axis=y"));
        Block.STRIPPED_SPRUCE_WOOD.addBlockAlternative(new BlockAlternative((short) 135, "axis=z"));
    }
}
