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
public final class DeepslateTileSlab {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19589, "type=top", "waterlogged=true"));
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19590, "type=top", "waterlogged=false"));
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19591, "type=bottom", "waterlogged=true"));
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19592, "type=bottom", "waterlogged=false"));
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19593, "type=double", "waterlogged=true"));
        Block.DEEPSLATE_TILE_SLAB.addBlockAlternative(new BlockAlternative((short) 19594, "type=double", "waterlogged=false"));
    }
}
