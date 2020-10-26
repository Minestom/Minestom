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
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1317, "powered=true", "shape=north_south"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1318, "powered=true", "shape=east_west"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1319, "powered=true", "shape=ascending_east"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1320, "powered=true", "shape=ascending_west"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1321, "powered=true", "shape=ascending_north"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1322, "powered=true", "shape=ascending_south"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1323, "powered=false", "shape=north_south"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1324, "powered=false", "shape=east_west"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1325, "powered=false", "shape=ascending_east"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1326, "powered=false", "shape=ascending_west"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1327, "powered=false", "shape=ascending_north"));
        Block.DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1328, "powered=false", "shape=ascending_south"));
    }
}
