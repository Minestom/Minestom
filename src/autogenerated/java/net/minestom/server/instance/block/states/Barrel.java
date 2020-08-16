package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Barrel {
	public static void initStates() {
		BARREL.addBlockAlternative(new BlockAlternative((short) 14795, "facing=north", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14796, "facing=north", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14797, "facing=east", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14798, "facing=east", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14799, "facing=south", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14800, "facing=south", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14801, "facing=west", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14802, "facing=west", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14803, "facing=up", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14804, "facing=up", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14805, "facing=down", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14806, "facing=down", "open=false"));
	}
}
