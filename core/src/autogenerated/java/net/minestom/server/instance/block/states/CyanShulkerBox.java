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
public final class CyanShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9336, "facing=north"));
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9337, "facing=east"));
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9338, "facing=south"));
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9339, "facing=west"));
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9340, "facing=up"));
        Block.CYAN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9341, "facing=down"));
    }
}
