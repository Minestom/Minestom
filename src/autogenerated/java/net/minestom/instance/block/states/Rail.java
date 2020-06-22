package net.minestom.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.instance.block.TmpBlock.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Rail {
	public static void initStates() {
		RAIL.addBlockAlternative(new BlockAlternative((short) 3643, "shape=north_south"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3644, "shape=east_west"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3645, "shape=ascending_east"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3646, "shape=ascending_west"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3647, "shape=ascending_north"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3648, "shape=ascending_south"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3649, "shape=south_east"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3650, "shape=south_west"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3651, "shape=north_west"));
		RAIL.addBlockAlternative(new BlockAlternative((short) 3652, "shape=north_east"));
	}
}
