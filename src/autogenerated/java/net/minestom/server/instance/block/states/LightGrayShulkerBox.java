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
public final class LightGrayShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9330, "facing=north"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9331, "facing=east"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9332, "facing=south"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9333, "facing=west"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9334, "facing=up"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9335, "facing=down"));
    }
}
