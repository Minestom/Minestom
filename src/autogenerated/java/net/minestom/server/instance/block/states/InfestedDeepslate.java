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
public final class InfestedDeepslate {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.INFESTED_DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 20333, "axis=x"));
        Block.INFESTED_DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 20334, "axis=y"));
        Block.INFESTED_DEEPSLATE.addBlockAlternative(new BlockAlternative((short) 20335, "axis=z"));
    }
}
