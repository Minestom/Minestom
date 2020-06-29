package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class PistonHead {
	public static void initStates() {
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1360, "facing=north", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1361, "facing=north", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1362, "facing=north", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1363, "facing=north", "short=false", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1364, "facing=east", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1365, "facing=east", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1366, "facing=east", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1367, "facing=east", "short=false", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1368, "facing=south", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1369, "facing=south", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1370, "facing=south", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1371, "facing=south", "short=false", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1372, "facing=west", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1373, "facing=west", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1374, "facing=west", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1375, "facing=west", "short=false", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1376, "facing=up", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1377, "facing=up", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1378, "facing=up", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1379, "facing=up", "short=false", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1380, "facing=down", "short=true", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1381, "facing=down", "short=true", "type=sticky"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1382, "facing=down", "short=false", "type=normal"));
		PISTON_HEAD.addBlockAlternative(new BlockAlternative((short) 1383, "facing=down", "short=false", "type=sticky"));
	}
}
