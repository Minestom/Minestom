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
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6946, "axis=x"));
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6947, "axis=y"));
        Block.QUARTZ_PILLAR.addBlockAlternative(new BlockAlternative((short) 6948, "axis=z"));
    }
}
