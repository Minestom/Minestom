package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class MovingPiston {
	public static void initStates() {
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1400, "facing=north", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1401, "facing=north", "type=sticky"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1402, "facing=east", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1403, "facing=east", "type=sticky"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1404, "facing=south", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1405, "facing=south", "type=sticky"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1406, "facing=west", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1407, "facing=west", "type=sticky"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1408, "facing=up", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1409, "facing=up", "type=sticky"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1410, "facing=down", "type=normal"));
		MOVING_PISTON.addBlockAlternative(new BlockAlternative((short) 1411, "facing=down", "type=sticky"));
	}
}
