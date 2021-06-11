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
public final class WhiteShulkerBox {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9528, "facing=north"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9529, "facing=east"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9530, "facing=south"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9531, "facing=west"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9532, "facing=up"));
        Block.WHITE_SHULKER_BOX.addBlockAlternative(new BlockAlternative((short) 9533, "facing=down"));
    }
}
