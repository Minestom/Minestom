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
public final class DamagedAnvil {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6622, "facing=north"));
        Block.DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6623, "facing=south"));
        Block.DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6624, "facing=west"));
        Block.DAMAGED_ANVIL.addBlockAlternative(new BlockAlternative((short) 6625, "facing=east"));
    }
}
