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
public final class OrangeShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9534, "facing=north"));
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9535, "facing=east"));
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9536, "facing=south"));
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9537, "facing=west"));
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9538, "facing=up"));
        Block.ORANGE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9539, "facing=down"));
    }
}
