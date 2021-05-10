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
public final class MagentaShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9294, "facing=north"));
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9295, "facing=east"));
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9296, "facing=south"));
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9297, "facing=west"));
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9298, "facing=up"));
        Block.MAGENTA_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9299, "facing=down"));
    }
}
