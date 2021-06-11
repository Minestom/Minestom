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
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9588, "facing=north"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9589, "facing=east"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9590, "facing=south"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9591, "facing=west"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9592, "facing=up"));
        Block.PURPLE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9593, "facing=down"));
    }
}
