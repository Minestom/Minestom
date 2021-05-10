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
public final class LimeShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9312, "facing=north"));
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9313, "facing=east"));
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9314, "facing=south"));
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9315, "facing=west"));
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9316, "facing=up"));
        Block.LIME_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9317, "facing=down"));
    }
}
