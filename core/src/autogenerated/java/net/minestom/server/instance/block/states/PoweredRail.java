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
public final class PoweredRail {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1305, "powered=true", "shape=north_south"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1306, "powered=true", "shape=east_west"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1307, "powered=true", "shape=ascending_east"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1308, "powered=true", "shape=ascending_west"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1309, "powered=true", "shape=ascending_north"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1310, "powered=true", "shape=ascending_south"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1311, "powered=false", "shape=north_south"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1312, "powered=false", "shape=east_west"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1313, "powered=false", "shape=ascending_east"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1314, "powered=false", "shape=ascending_west"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1315, "powered=false", "shape=ascending_north"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1316, "powered=false", "shape=ascending_south"));
    }
}
