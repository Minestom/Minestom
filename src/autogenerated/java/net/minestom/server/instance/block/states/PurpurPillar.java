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
public final class PurpurPillar {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9139, "axis=x"));
        Block.PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9140, "axis=y"));
        Block.PURPUR_PILLAR.addBlockAlternative(new BlockAlternative((short) 9141, "axis=z"));
    }
}
