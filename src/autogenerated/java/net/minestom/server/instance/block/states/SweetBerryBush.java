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
public final class SweetBerryBush {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 15208, "age=0"));
        Block.SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 15209, "age=1"));
        Block.SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 15210, "age=2"));
        Block.SWEET_BERRY_BUSH.addBlockAlternative(new BlockAlternative((short) 15211, "age=3"));
    }
}
