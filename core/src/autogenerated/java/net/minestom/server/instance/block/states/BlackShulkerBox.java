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
public final class BlackShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9372, "facing=north"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9373, "facing=east"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9374, "facing=south"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9375, "facing=west"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9376, "facing=up"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9377, "facing=down"));
    }
}
