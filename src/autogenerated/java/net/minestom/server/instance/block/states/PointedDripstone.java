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
public final class PointedDripstone {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18544, "thickness=tip_merge", "vertical_direction=up", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18545, "thickness=tip_merge", "vertical_direction=up", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18546, "thickness=tip_merge", "vertical_direction=down", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18547, "thickness=tip_merge", "vertical_direction=down", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18548, "thickness=tip", "vertical_direction=up", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18549, "thickness=tip", "vertical_direction=up", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18550, "thickness=tip", "vertical_direction=down", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18551, "thickness=tip", "vertical_direction=down", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18552, "thickness=frustum", "vertical_direction=up", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18553, "thickness=frustum", "vertical_direction=up", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18554, "thickness=frustum", "vertical_direction=down", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18555, "thickness=frustum", "vertical_direction=down", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18556, "thickness=middle", "vertical_direction=up", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18557, "thickness=middle", "vertical_direction=up", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18558, "thickness=middle", "vertical_direction=down", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18559, "thickness=middle", "vertical_direction=down", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18560, "thickness=base", "vertical_direction=up", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18561, "thickness=base", "vertical_direction=up", "waterlogged=false"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18562, "thickness=base", "vertical_direction=down", "waterlogged=true"));
        Block.POINTED_DRIPSTONE.addBlockAlternative(new BlockAlternative((short) 18563, "thickness=base", "vertical_direction=down", "waterlogged=false"));
    }
}
