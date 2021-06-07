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
public final class StructureBlock {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15989, "mode=save"));
        Block.STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15990, "mode=load"));
        Block.STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15991, "mode=corner"));
        Block.STRUCTURE_BLOCK.addBlockAlternative(new BlockAlternative((short) 15992, "mode=data"));
    }
}
