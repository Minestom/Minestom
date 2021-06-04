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
public final class Lava {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 50, "level=0"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 51, "level=1"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 52, "level=2"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 53, "level=3"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 54, "level=4"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 55, "level=5"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 56, "level=6"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 57, "level=7"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 58, "level=8"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 59, "level=9"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 60, "level=10"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 61, "level=11"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 62, "level=12"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 63, "level=13"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 64, "level=14"));
        Block.LAVA.addBlockAlternative(new BlockAlternative((short) 65, "level=15"));
    }
}
