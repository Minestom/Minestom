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
public final class GreenShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9606, "facing=north"));
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9607, "facing=east"));
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9608, "facing=south"));
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9609, "facing=west"));
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9610, "facing=up"));
        Block.GREEN_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9611, "facing=down"));
    }
}
