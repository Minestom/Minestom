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
public final class GrayShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9570, "facing=north"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9571, "facing=east"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9572, "facing=south"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9573, "facing=west"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9574, "facing=up"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9575, "facing=down"));
    }
}
