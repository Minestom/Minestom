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
public final class RedShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9366, "facing=north"));
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9367, "facing=east"));
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9368, "facing=south"));
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9369, "facing=west"));
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9370, "facing=up"));
        Block.RED_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9371, "facing=down"));
    }
}
