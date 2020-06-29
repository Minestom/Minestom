package net.minestom.server.instance.block.states;
import net.minestom.server.instance.block.BlockAlternative;
import static net.minestom.server.instance.block.Block.*;
/**
 * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.
 */
@Deprecated(forRemoval = false, since = "forever")
public class Barrel {
	public static void initStates() {
		BARREL.addBlockAlternative(new BlockAlternative((short) 11135, "facing=north", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11136, "facing=north", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11137, "facing=east", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11138, "facing=east", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11139, "facing=south", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11140, "facing=south", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11141, "facing=west", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11142, "facing=west", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11143, "facing=up", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11144, "facing=up", "open=false"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11145, "facing=down", "open=true"));
		BARREL.addBlockAlternative(new BlockAlternative((short) 11146, "facing=down", "open=false"));
	}
}
