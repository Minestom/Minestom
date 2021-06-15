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
public final class BlueShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9594, "facing=north"));
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9595, "facing=east"));
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9596, "facing=south"));
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9597, "facing=west"));
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9598, "facing=up"));
        Block.BLUE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9599, "facing=down"));
    }
}
