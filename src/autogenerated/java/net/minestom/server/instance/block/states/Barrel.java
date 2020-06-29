package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Barrel {
	public static void initStates() {
		BARREL.addBlockAlternative(new BlockAlternative((short) 14791, "facing=north", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14792, "facing=north", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14793, "facing=east", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14794, "facing=east", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14795, "facing=south", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14796, "facing=south", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14797, "facing=west", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14798, "facing=west", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14799, "facing=up", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14800, "facing=up", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14801, "facing=down", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 14802, "facing=down", "open=false"));
	}
}
