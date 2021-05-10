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
public final class Cauldron {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.CAULDRON.addBlockAlternative(new BlockAlternative((short) 5145, "level=0"));
        Block.CAULDRON.addBlockAlternative(new BlockAlternative((short) 5146, "level=1"));
        Block.CAULDRON.addBlockAlternative(new BlockAlternative((short) 5147, "level=2"));
        Block.CAULDRON.addBlockAlternative(new BlockAlternative((short) 5148, "level=3"));
    }
}
