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
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15759, "level=0"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15760, "level=1"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15761, "level=2"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15762, "level=3"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15763, "level=4"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15764, "level=5"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15765, "level=6"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15766, "level=7"));
        Block.COMPOSTER.addBlockAlternative(new BlockAlternative((short) 15767, "level=8"));
    }
}
