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
public final class Target {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16014, "power=0"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16015, "power=1"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16016, "power=2"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16017, "power=3"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16018, "power=4"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16019, "power=5"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16020, "power=6"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16021, "power=7"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16022, "power=8"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16023, "power=9"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16024, "power=10"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16025, "power=11"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16026, "power=12"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16027, "power=13"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16028, "power=14"));
        Block.TARGET.addBlockAlternative(new BlockAlternative((short) 16029, "power=15"));
    }
}
