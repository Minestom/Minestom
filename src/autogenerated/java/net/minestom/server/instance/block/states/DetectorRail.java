package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class DetectorRail {
	public static void initStates() {
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1317, "powered=true", "shape=north_south"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1318, "powered=true", "shape=east_west"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1319, "powered=true", "shape=ascending_east"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1320, "powered=true", "shape=ascending_west"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1321, "powered=true", "shape=ascending_north"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1322, "powered=true", "shape=ascending_south"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1323, "powered=false", "shape=north_south"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1324, "powered=false", "shape=east_west"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1325, "powered=false", "shape=ascending_east"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1326, "powered=false", "shape=ascending_west"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1327, "powered=false", "shape=ascending_north"));
		DETECTOR_RAIL.addBlockAlternative(new BlockAlternative((short) 1328, "powered=false", "shape=ascending_south"));
	}
}
