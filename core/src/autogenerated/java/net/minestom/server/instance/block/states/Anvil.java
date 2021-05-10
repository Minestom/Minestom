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
public final class Anvil {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.ANVIL.addBlockAlternative(new BlockAlternative((short) 6614, "facing=north"));
        Block.ANVIL.addBlockAlternative(new BlockAlternative((short) 6615, "facing=south"));
        Block.ANVIL.addBlockAlternative(new BlockAlternative((short) 6616, "facing=west"));
        Block.ANVIL.addBlockAlternative(new BlockAlternative((short) 6617, "facing=east"));
    }
}
