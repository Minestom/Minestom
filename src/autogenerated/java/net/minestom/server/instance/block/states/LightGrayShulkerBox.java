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
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9576, "facing=north"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9577, "facing=east"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9578, "facing=south"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9579, "facing=west"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9580, "facing=up"));
        Block.LIGHT_GRAY_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9581, "facing=down"));
    }
}
