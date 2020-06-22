package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class ActivatorRail {
	public static void initStates() {
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6287, "powered=true", "shape=north_south"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6288, "powered=true", "shape=east_west"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6289, "powered=true", "shape=ascending_east"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6290, "powered=true", "shape=ascending_west"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6291, "powered=true", "shape=ascending_north"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6292, "powered=true", "shape=ascending_south"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6293, "powered=false", "shape=north_south"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6294, "powered=false", "shape=east_west"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6295, "powered=false", "shape=ascending_east"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6296, "powered=false", "shape=ascending_west"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6297, "powered=false", "shape=ascending_north"));
		ACTIVATOR_RAIL.addBlockAlternative(new BlockAlternative((short) 6298, "powered=false", "shape=ascending_south"));
	}
}
