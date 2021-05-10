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
public final class ChippedAnvil {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6618, "facing=north"));
        Block.CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6619, "facing=south"));
        Block.CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6620, "facing=west"));
        Block.CHIPPED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6621, "facing=east"));
    }
}
