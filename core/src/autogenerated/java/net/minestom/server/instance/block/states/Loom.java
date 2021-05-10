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
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 14791, "facing=north"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 14792, "facing=south"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 14793, "facing=west"));
        Block.LOOM.addBlockAlternative(new BlockAlternative((short) 14794, "facing=east"));
    }
}
