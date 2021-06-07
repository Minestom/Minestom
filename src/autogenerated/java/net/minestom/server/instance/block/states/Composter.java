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
public final class Composter {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16005, "level=0"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16006, "level=1"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16007, "level=2"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16008, "level=3"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16009, "level=4"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16010, "level=5"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16011, "level=6"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16012, "level=7"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 16013, "level=8"));
    }
}
