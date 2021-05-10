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
public final class PinkShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9318, "facing=north"));
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9319, "facing=east"));
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9320, "facing=south"));
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9321, "facing=west"));
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9322, "facing=up"));
        Block.PINK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9323, "facing=down"));
    }
}
