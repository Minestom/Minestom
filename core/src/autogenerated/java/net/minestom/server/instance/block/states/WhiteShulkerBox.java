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
public final class WhiteShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9282, "facing=north"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9283, "facing=east"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9284, "facing=south"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9285, "facing=west"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9286, "facing=up"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9287, "facing=down"));
    }
}
