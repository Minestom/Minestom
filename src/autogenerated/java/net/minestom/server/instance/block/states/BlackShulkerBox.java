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
public final class BlackShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9618, "facing=north"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9619, "facing=east"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9620, "facing=south"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9621, "facing=west"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9622, "facing=up"));
        Block.BLACK_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9623, "facing=down"));
    }
}
