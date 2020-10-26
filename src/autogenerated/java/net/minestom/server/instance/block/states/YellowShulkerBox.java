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
public final class YellowShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9306, "facing=north"));
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9307, "facing=east"));
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9308, "facing=south"));
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9309, "facing=west"));
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9310, "facing=up"));
        Block.YELLOW_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9311, "facing=down"));
    }
}
