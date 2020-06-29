package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Piston {
	public static void initStates() {
		PISTON.addBlockAlternative(new BlockAlternative((short) 1348, "extended=true", "facing=north"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1349, "extended=true", "facing=east"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1350, "extended=true", "facing=south"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1351, "extended=true", "facing=west"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1352, "extended=true", "facing=up"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1353, "extended=true", "facing=down"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1354, "extended=false", "facing=north"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1355, "extended=false", "facing=east"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1356, "extended=false", "facing=south"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1357, "extended=false", "facing=west"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1358, "extended=false", "facing=up"));
		PISTON.addBlockAlternative(new BlockAlternative((short) 1359, "extended=false", "facing=down"));
	}
}
