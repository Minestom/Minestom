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
public final class ShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9522, "facing=north"));
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9523, "facing=east"));
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9524, "facing=south"));
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9525, "facing=west"));
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9526, "facing=up"));
        Block.SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9527, "facing=down"));
    }
}
