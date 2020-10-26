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
public final class BrownShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9354, "facing=north"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9355, "facing=east"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9356, "facing=south"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9357, "facing=west"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9358, "facing=up"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9359, "facing=down"));
    }
}
