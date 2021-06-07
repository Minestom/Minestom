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
public final class Rail {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3702, "shape=north_south", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3703, "shape=north_south", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3704, "shape=east_west", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3705, "shape=east_west", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3706, "shape=ascending_east", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3707, "shape=ascending_east", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3708, "shape=ascending_west", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3709, "shape=ascending_west", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3710, "shape=ascending_north", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3711, "shape=ascending_north", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3712, "shape=ascending_south", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3713, "shape=ascending_south", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3714, "shape=south_east", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3715, "shape=south_east", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3716, "shape=south_west", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3717, "shape=south_west", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3718, "shape=north_west", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3719, "shape=north_west", "waterlogged=false"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3720, "shape=north_east", "waterlogged=true"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3721, "shape=north_east", "waterlogged=false"));
    }
}
