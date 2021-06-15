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
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1337, "powered=true", "shape=north_south", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1338, "powered=true", "shape=north_south", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1339, "powered=true", "shape=east_west", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1340, "powered=true", "shape=east_west", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1341, "powered=true", "shape=ascending_east", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1342, "powered=true", "shape=ascending_east", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1343, "powered=true", "shape=ascending_west", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1344, "powered=true", "shape=ascending_west", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1345, "powered=true", "shape=ascending_north", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1346, "powered=true", "shape=ascending_north", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1347, "powered=true", "shape=ascending_south", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1348, "powered=true", "shape=ascending_south", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1349, "powered=false", "shape=north_south", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1350, "powered=false", "shape=north_south", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1351, "powered=false", "shape=east_west", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1352, "powered=false", "shape=east_west", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1353, "powered=false", "shape=ascending_east", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1354, "powered=false", "shape=ascending_east", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1355, "powered=false", "shape=ascending_west", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1356, "powered=false", "shape=ascending_west", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1357, "powered=false", "shape=ascending_north", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1358, "powered=false", "shape=ascending_north", "waterlogged=false"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1359, "powered=false", "shape=ascending_south", "waterlogged=true"));
        Block.POWERED_RAIL.addBlockAlternative(new BlockAlternative((short) 1360, "powered=false", "shape=ascending_south", "waterlogged=false"));
    }
}
