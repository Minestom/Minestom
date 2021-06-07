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
public final class BoneBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9506, "axis=x"));
        Block.BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9507, "axis=y"));
        Block.BONE_BLOCK.addBlockAlternative(new BlockAlternative((short) 9508, "axis=z"));
    }
}
