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
public final class PurpleShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9342, "facing=north"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9343, "facing=east"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9344, "facing=south"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9345, "facing=west"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9346, "facing=up"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9347, "facing=down"));
    }
}
