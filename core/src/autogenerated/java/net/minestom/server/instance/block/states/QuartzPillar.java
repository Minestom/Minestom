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
public final class QuartzPillar {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6744, "axis=x"));
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6745, "axis=y"));
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6746, "axis=z"));
    }
}
