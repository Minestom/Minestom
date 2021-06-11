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
public final class Loom {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 15037, "facing=north"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 15038, "facing=south"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 15039, "facing=west"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 15040, "facing=east"));
    }
}
