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
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9324, "facing=north"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9325, "facing=east"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9326, "facing=south"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9327, "facing=west"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9328, "facing=up"));
        Block.GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9329, "facing=down"));
    }
}
