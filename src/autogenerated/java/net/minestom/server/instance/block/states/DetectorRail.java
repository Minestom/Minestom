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
public final class DetectorRail {
    /**
     * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
     */
    @Deprecated(
            since = "forever",
            forRemoval = false
    )
    public static void initStates() {
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1361, "powered=true", "shape=north_south", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1362, "powered=true", "shape=north_south", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1363, "powered=true", "shape=east_west", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1364, "powered=true", "shape=east_west", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1365, "powered=true", "shape=ascending_east", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1366, "powered=true", "shape=ascending_east", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1367, "powered=true", "shape=ascending_west", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1368, "powered=true", "shape=ascending_west", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1369, "powered=true", "shape=ascending_north", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1370, "powered=true", "shape=ascending_north", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1371, "powered=true", "shape=ascending_south", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1372, "powered=true", "shape=ascending_south", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1373, "powered=false", "shape=north_south", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1374, "powered=false", "shape=north_south", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1375, "powered=false", "shape=east_west", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1376, "powered=false", "shape=east_west", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1377, "powered=false", "shape=ascending_east", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1378, "powered=false", "shape=ascending_east", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1379, "powered=false", "shape=ascending_west", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1380, "powered=false", "shape=ascending_west", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1381, "powered=false", "shape=ascending_north", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1382, "powered=false", "shape=ascending_north", "waterlogged=false"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1383, "powered=false", "shape=ascending_south", "waterlogged=true"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1384, "powered=false", "shape=ascending_south", "waterlogged=false"));
    }
}
