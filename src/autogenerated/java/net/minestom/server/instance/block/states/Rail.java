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
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3645, "shape=north_south"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3646, "shape=east_west"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3647, "shape=ascending_east"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3648, "shape=ascending_west"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3649, "shape=ascending_north"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3650, "shape=ascending_south"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3651, "shape=south_east"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3652, "shape=south_west"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3653, "shape=north_west"));
        Block.RAIL.addBlockAlternative(new BlockAlternative((short) 3654, "shape=north_east"));
    }
}
