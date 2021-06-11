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
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9600, "facing=north"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9601, "facing=east"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9602, "facing=south"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9603, "facing=west"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9604, "facing=up"));
        Block.BROWN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9605, "facing=down"));
    }
}
