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
public final class PumpkinStem {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4776, "age=0"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4777, "age=1"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4778, "age=2"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4779, "age=3"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4780, "age=4"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4781, "age=5"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4782, "age=6"));
        Block.PUMPKIN_STEM.addBlockAlternative(new BlockAlternative((short) 4783, "age=7"));
    }
}
