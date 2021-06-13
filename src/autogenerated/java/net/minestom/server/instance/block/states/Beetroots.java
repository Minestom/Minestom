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
public final class Beetroots {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BEETROOTS.addBlockAlternative(new BlockAlternative((short) 9469, "age=0"));
        Block.BEETROOTS.addBlockAlternative(new BlockAlternative((short) 9470, "age=1"));
        Block.BEETROOTS.addBlockAlternative(new BlockAlternative((short) 9471, "age=2"));
        Block.BEETROOTS.addBlockAlternative(new BlockAlternative((short) 9472, "age=3"));
    }
}
