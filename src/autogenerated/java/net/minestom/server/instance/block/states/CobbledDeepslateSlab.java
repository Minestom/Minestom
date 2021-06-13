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
public final class CobbledDeepslateSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18767, "type=top", "waterlogged=true"));
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18768, "type=top", "waterlogged=false"));
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18769, "type=bottom", "waterlogged=true"));
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18770, "type=bottom", "waterlogged=false"));
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18771, "type=double", "waterlogged=true"));
        Block.COBBLED_DEEPSLATE_SLAB.addBlockAlternative(new BlockAlternative((short) 18772, "type=double", "waterlogged=false"));
    }
}
